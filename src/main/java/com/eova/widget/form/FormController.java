/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.widget.form;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;



import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.Easy;
import com.eova.common.Response;
import com.eova.common.utils.xx;
import com.eova.common.utils.io.FileUtil;
import com.eova.common.utils.io.FileUtils;
import com.eova.common.utils.string.PinyinTool;
import com.eova.common.utils.util.GsonUtil;
import com.eova.config.EovaConst;
import com.eova.config.PageConst;
import com.eova.engine.DynamicParse;
import com.eova.model.EovaLog;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.config.TemplateConfig;
import com.eova.template.common.util.TemplateUtil;
import com.eova.widget.WidgetManager;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;

/**
 * Form组件
 * 
 * @author Jieven
 * 
 */
public class FormController extends Controller {

	final Controller ctrl = this;

	/** 自定义拦截器 **/
	protected MetaObjectIntercept intercept = null;

	/** 异常信息 **/
	private String errorInfo = "";

	/** 当前操作的主对象 **/
	private final Record record = new Record();
	/** 
	 * VIEW_FORM 指定生效的view形式,否则走 之前的默认规则 updateForm/addForm ,form
	 * 同时form 指定了是否View强制生效,而不是数据配置的隐藏，显示，禁止
	 *  优先从update起手修改吧
	 *  **/
	public static final String VIEW_FORM = "VIEW_FORM";
	
	
	
	public void add() throws Exception {
		String objectCode = this.getPara(0);
		
		MetaObject object = sm.meta.getMeta(objectCode);
		// 字段禁用默认对新增无效(没有这个说法了)，此处主要处理可能出现的beetl
		for (MetaField mf : object.getFields()) {
			//mf.put("is_disable", false);
			
			//检查是否beetl默认值
			String defaulter = mf.getStr("defaulter");
			defaulter = DynamicParse.buildSql(defaulter, getSessionAttr(EovaConst.USER));
			mf.set("defaulter", defaulter);
		}

		// 构建关联参数值
		Record fixed = WidgetManager.getRef(this);
		
		// 业务拦截
		intercept = TemplateUtil.initIntercept(object.getBizIntercept());
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl);
			ac.fixed = fixed;
			intercept.addInit(ac);
		}
		Object o = object.getConfig();
		setAttr("fixed", fixed.toJson());
		setAttr("object", object);
	
		
		returnUrl("add");
	}

	public void doAdd() throws Exception {

		String objectCode = this.getPara(0);

		final MetaObject object = sm.meta.getMeta(objectCode);
		
		

		// 获取当前操作数据
		WidgetManager.buildData(this, object, record, object.getPk(), true);

		intercept = TemplateUtil.initIntercept(object.getBizIntercept());
		// 事务(默认为TRANSACTION_READ_COMMITTED)
		boolean flag = Db.use(object.getDs()).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					AopContext ac = new AopContext(ctrl, record,object);

					// 新增前置任务
					if (intercept != null) {
						String msg = intercept.addBefore(ac);
						if (!xx.isEmpty(msg)) {
							errorInfo = msg;
							return false;
						}
					}

					if (!xx.isEmpty(object.getTable())) {
						// add table
						Db.use(object.getDs()).save(object.getTable(), object.getPk(), record);
					} else {
						// update view
						 WidgetManager.operateView(TemplateConfig.ADD, object, record);
						// 视图无法自动操作，请自定义元对象业务拦截完成持久化逻辑！
					}

					// 新增后置任务
					if (intercept != null) {
						String msg = intercept.addAfter(ac);
						if (!xx.isEmpty(msg)) {
							errorInfo = msg;
							return false;
						}
					}
				} catch (Exception e) {
					errorInfo = "新增异常:" + TemplateUtil.buildException(e);
					return false;
				}
				return true;
			}
		});

		// AOP提示信息
		if (!flag) {
			renderJson(new Easy(errorInfo));
			return;
		}

		// 记录新增日志
		EovaLog.dao.info(this, EovaLog.ADD, object.getStr("code"));
		
		errorInfo = null;
		// 新增成功之后
		if (intercept != null) {
			try {
				ArrayList<Record> records = new ArrayList<Record>();
				records.add(record);

				AopContext ac = new AopContext(this, records,object);
				String msg = intercept.addSucceed(ac);
				if (!xx.isEmpty(msg)) {
					errorInfo = msg;
				}
			} catch (Exception e) {
				errorInfo = TemplateUtil.buildException(e);
				renderJson(new Easy("新增成功,addSucceed拦截执行异常!" + errorInfo));
				return;
			}
		}
		
		renderJson(new Easy(errorInfo,true));
	}

	public void update() throws Exception {
		
		// 获取关联参数
		Record fixed = WidgetManager.getRef(this);
				
		AopContext ac = new AopContext(ctrl);
		ac.fixed = fixed;
		
		// 初始化数据
		buildFormData(true, ac);
		// 业务拦截
		intercept = TemplateUtil.initIntercept(ac.object.getBizIntercept());
		if (intercept != null) {
			intercept.updateInit(ac);
		}
		
		setAttr("fixed", fixed.toJson());
		
		returnUrl("update");
		
	}

	public void doUpdate() throws Exception {

		String objectCode = this.getPara(0);

		final MetaObject object = sm.meta.getMeta(objectCode);

		// 获取当前操作数据
		WidgetManager.buildData(this, object, record, object.getPk(), false);
		
		final Object pkValue = record.get(object.getPk());

		intercept = TemplateUtil.initIntercept(object.getBizIntercept());
		// 事务(默认为TRANSACTION_READ_COMMITTED)
		boolean flag = Db.use(object.getDs()).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					AopContext ac = new AopContext(ctrl, record,object);

					// 修改前置任务
					if (intercept != null) {
						String msg = intercept.updateBefore(ac);
						if (!xx.isEmpty(msg)) {
							errorInfo = msg;
							return false;
						}
					}

					if (!xx.isEmpty(object.getTable())) {
						// update table
						Db.use(object.getDs()).update(object.getTable(), object.getPk(), record);
					} else {
						// update view
						 WidgetManager.operateView(TemplateConfig.UPDATE, object, record);
						// 视图无法自动操作，请自定义元对象业务拦截完成持久化逻辑！
					}

					// 修改后置任务
					if (intercept != null) {
						String msg = intercept.updateAfter(ac);
						if (!xx.isEmpty(msg)) {
							errorInfo = msg;
						}
					}
				} catch (Exception e) {
					errorInfo = "修改异常:" + TemplateUtil.buildException(e);
					return false;
				}
				return true;
			}

		});

		// AOP提示信息
		if (!flag) {
			renderJson(new Easy(errorInfo));
			return;
		}

		// 记录新增日志
		EovaLog.dao.info(this, EovaLog.UPDATE, object.getStr("code") + "[" + pkValue + "]");
		
		intercept = null;
		// 修改成功之后
		if (intercept != null) {
			try {
				ArrayList<Record> records = new ArrayList<Record>();
				records.add(record);

				AopContext ac = new AopContext(this, records,object);
				String msg = intercept.updateSucceed(ac);
				if (!xx.isEmpty(msg)) {
					errorInfo = msg;
				}
			} catch (Exception e) {
				errorInfo = TemplateUtil.buildException(e);
				renderJson(new Easy("修改成功,updateSucceed拦截执行异常!" + errorInfo));
				return;
			}
		}

		renderJson(new Easy(errorInfo,true));
	}

	public void detail() throws Exception{
		AopContext ac = new AopContext(ctrl);
		buildFormData(false, ac);
	
		String objectCode = this.getPara(0);
		final MetaObject object = sm.meta.getMeta(objectCode);
		intercept = TemplateUtil.initIntercept(object.getBizIntercept());
		if (intercept != null) {
			String msg = intercept.detailAfter(ac);
			if (!xx.isEmpty(msg)) {
				
				renderJson(new Response(Response.STATUS_FAIL,"查看后置拦截执行异常!" + msg));
				return;
			}
		}
		
		returnUrl("detail");
	}

	public void detailJson() throws Exception{
		String objectCode = this.getPara(0);
		final MetaObject object = sm.meta.getMeta(objectCode);
		
		intercept = TemplateUtil.initIntercept(object.getBizIntercept());
		
		AopContext ac = new AopContext(ctrl);
		buildFormData(false, ac);
	
		
		
		// 查看后置任务
				if (intercept != null) {
					String msg = intercept.detailAfter(ac);
					if (!xx.isEmpty(msg)) {
						
						renderJson(new Response(Response.STATUS_FAIL,"查看后置拦截执行异常!" + msg));
						return;
					}
				}

		
		renderJson(new Response(this.getAttr("record")));
	}
	
	/**
	 * 设计表单，
	 * @throws Exception
	 */
	public void design() throws Exception{
		String objectCode = this.getPara(0);
		 
		
		final MetaObject object = sm.meta.getMeta(objectCode);
		setAttr("object", object);
		if(!xx.isEmpty(this.getPara("formType")))
			setAttr("formType", this.getPara("formType"));
		returnUrl("design");
	}
	
	public void designField() throws Exception{
		String objectCode = this.getPara(0);
		String field = this.getPara(1);
		final MetaObject object = sm.meta.getMeta(objectCode);
		
		List<MetaField> fs = object.getFields();
		for(Iterator<MetaField> it = fs.iterator();it.hasNext();){
			MetaField f =it.next();
			if(f.getEn().equalsIgnoreCase(field)){
				setAttr("f", f);
				break;
			}
		}
	
		
		returnUrl("design_field");
	}
	
	/**
	 * 保存表单
	 * 如果存在则替换 ,为了防止和别的配置冲突，后缀必须是Form（不需要区分大小写）,本处不校验了
	 * @throws Exception
	 */
	public void saveDesign() throws Exception{
		String objectCode = this.getPara(0);//code
		String formName = this.getPara(1);//name
		final MetaObject object = sm.meta.getMeta(objectCode);
		
		String reqJson =  HttpKit.readData(getRequest()) ;
		
		String configStr = object.getStr("config");
		
		Map config = null;
		if(xx.isEmpty(configStr)) {
			config = new LinkedHashMap();
			Map form = new HashMap();
			form.put("force", 2);
			form.put("list", GsonUtil.GsonToList(reqJson, Map.class));
			config.put(formName, form);
		}else {
			 config = GsonUtil.GsonToMaps(configStr);
			 Map form = new HashMap();
				form.put("force", 2);
				form.put("list", GsonUtil.GsonToList(reqJson, Map.class));
			 config.put(formName, form);
		}
		
		object.set("config", GsonUtil.GsonString(config));
		object.update();
		
		renderJson(new Response());
	}
	
	
	/**
	 * 目前除了会跳转到指定模板页面，还会生成固定页面供给修改
	 * @param fun
	 */
	private void returnUrl(String fun) {

		// 跳转页面,如果存在指定的页面，则指向：base+菜单+指定页面
		String page = this.getPara("page");//即可指定 页面name，也告诉页面类型  ，，，比如页面叫shenhe 则跳转至shenhe.html页面，同时告知页面此功能叫：shenhe
		String menuCode = this.getPara("menuCode");
		String objectCode = this.getPara(0);
		
		if(!xx.isEmpty(this.getPara(VIEW_FORM)))
			setAttr(VIEW_FORM, this.getPara(VIEW_FORM));
		
		if (!xx.isEmpty(page) && !xx.isEmpty(menuCode)) {// 主要是用系统生成的页面
			{
				//绑定对象的默认值（如果对象字段为空的话）
				MetaObject object = this.getAttr("object");
				Record record = this.getAttr("record");
				if(object != null && record != null){
					List<MetaField> fs = object.getFields();
					String[] keys = record.getColumnNames();
					for(String one:keys){
						if(record.get(one) == null){
							//如果有默认值
							//var fields =object.fields; 
							MetaField target = null;
							 for(MetaField f:fs){
								 if(f.getEn().equalsIgnoreCase(one)){
									 target = f;
									 break;
								 }	 
							 }
							 if(target != null 
									 &&!xx.isEmpty(target.getStr("defaulter")))
								 record.set(one, target.getStr("defaulter"));
						}
					}
				}
				
			}
			
			
			setAttr(EovaConst.PAGE_TYPE, page);
			String base = xx.getConfig("page_default", "/");
			render(base + menuCode + "/" + objectCode + "/" + page + ".html");
			
		} else {
			if(!xx.isEmpty(this.getPara(VIEW_FORM))){//xxForm
				int p = this.getPara(VIEW_FORM).toLowerCase().indexOf("form");
				if(p>0){
					setAttr(EovaConst.PAGE_TYPE, this.getPara(VIEW_FORM).substring(0,p));
				}else{
					setAttr(EovaConst.PAGE_TYPE, this.getPara(VIEW_FORM));
				}
				
			}else
				setAttr(EovaConst.PAGE_TYPE, fun);
			
			String template = this.getPara(PageConst.Template);// 支持模板//模板
			if (!xx.isEmpty(template))
				render("/eova/widget/form/" + fun + "_" + template + ".html");
			else
				render("/eova/widget/form/" + fun + ".html");
		}

	}
	
	
	/**
	 * 构建对象数据
	 */
	private MetaObject buildFormData(boolean isEdit, AopContext ac) {
		String objectCode = this.getPara(0);
		// 获取主键的值
		Object pkValue = getPara(1);
		
		// 是否是UniqueIdentifier类型 XXXX-XXXX-XXXX
//		if((getPara(1).length()==8)&&(getPara(2).length()==4)&&(getPara(3).length()==4)&&(getPara(4).length()==4)&&(getPara(5).length()==12)){
//			pkValue = getPara(1).toString()+"-"+getPara(2).toString()+"-"+getPara(3).toString()+"-"+getPara(4).toString()+"-"+getPara(5).toString();
//		}
		
		MetaObject object = sm.meta.getMeta(objectCode);
		// 此处主要处理可能出现的beetl
		for (MetaField mf : object.getFields()) {
					//mf.put("is_disable", false);
					
					//检查是否beetl默认值
					String defaulter = mf.getStr("defaulter");
					defaulter = DynamicParse.buildSql(defaulter, getSessionAttr(EovaConst.USER));
					mf.set("defaulter", defaulter);
		}

		// 根据主键获取对象
		Record record = null;
		if(!xx.isEmpty(pkValue))
		 record = Db.use(object.getDs()).findById(object.getView(), object.getPk(), pkValue);
		
		if(record == null){
			setAttr("model", true);
			record = new Record();
		}
			
		
		setAttr("record", record);
		setAttr("object", object);

		ac.object = object;
		ac.record = record;

		return object;
	}
	
	

}