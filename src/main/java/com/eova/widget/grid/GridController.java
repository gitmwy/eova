/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.widget.grid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.Easy;
import com.eova.common.render.XlsRender;
import com.eova.common.utils.xx;
import com.eova.common.utils.db.DbUtil;
import com.eova.config.EovaConst;
import com.eova.config.PageConst;
import com.eova.model.EovaLog;
import com.eova.model.Menu;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.config.TemplateConfig;
import com.eova.template.common.util.TemplateUtil;
import com.eova.widget.WidgetManager;
import com.eova.widget.WidgetUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * Grid组件
 * 
 * @author Jieven
 * 
 */
public class GridController extends Controller {

	final Controller ctrl = this;

	/** 元对象业务拦截器 **/
	protected MetaObjectIntercept intercept = null;

	/** 异常信息 **/
	private String errorInfo = "";

	/**
	 * 导出查询(jxl导出比较丑，如果需要漂亮请使用模板导出，通用导出未实现)
	 * 
	 * @throws Exception
	 */
	public void export() throws Exception {
		String objectCode = getPara(0);
		String menuCode = getPara(1);

		MetaObject object = sm.meta.getMeta(objectCode);
		Menu menu = Menu.dao.findByCode(menuCode);

		intercept = TemplateUtil.initIntercept(object.getBizIntercept());

		// 构建查询
		List<Object> parmList = new ArrayList<Object>();
		String sql = WidgetManager.buildQuery(ctrl, object, menu, intercept, parmList);

		// 转换SQL参数
		Object[] paras = new Object[parmList.size()];
		parmList.toArray(paras);
		List<Record> data = Db.use(object.getDs()).find("select *" + DbUtil.formatSql(sql), paras);

		List<MetaField> fields = object.getFields();
		
		// 根据表达式将数据中的值翻译成汉字
		WidgetManager.convertValueByExp(this, fields, data);

		Iterator<MetaField> it = fields.iterator();  
		while (it.hasNext()) {
			MetaField f = it.next();
			if (!f.getBoolean("is_show")) {
				it.remove();
			}
		}
		
		render(new XlsRender(data, fields, object));
	}

	/**
	 * 分页查询
	 * 
	 * @throws Exception
	 */
	public void query() throws Exception {

		String objectCode = getPara(0);
		String menuCode = getPara(1);
		int pageNumber = getParaToInt(PageConst.PAGENUM, 1);
		int pageSize = getParaToInt(PageConst.PAGESIZE, 10000);

		MetaObject object = sm.meta.getMeta(objectCode);
		Menu menu = Menu.dao.findByCode(menuCode);

		intercept = TemplateUtil.initIntercept(object.getBizIntercept());

		// 构建查询
		List<Object> parmList = new ArrayList<Object>();
		String sql = WidgetManager.buildQuery(ctrl, object, menu, intercept, parmList);

		// 转换SQL参数
		Object[] paras = new Object[parmList.size()];
		parmList.toArray(paras);
		Page<Record> page = Db.use(object.getDs()).paginate(pageNumber, pageSize, "select *", DbUtil.formatSql(sql), paras);

		// 查询后置任务
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl, page.getList(),object);
			intercept.queryAfter(ac);
		}

		// 备份Value列，然后将值列转换成Key列
		WidgetUtil.copyValueColumn(page.getList(), object.getPk(), object.getFields());
		// 根据表达式将数据中的值翻译成汉字
		WidgetManager.convertValueByExp(this, object.getFields(), page.getList());

		// 将分页数据转换成JSON
		String json = JsonKit.toJson(page.getList());
		json = "{\"status\": 200,\"total\":" + page.getTotalRow() + ",\"rows\":" + json;
		
		//layui
//		json = "{\"count\":" + page.getTotalRow() + ",\"data\":" + json;
		
		// if (objectCode.equals("yimei_product_order")) {
		// json += ",\"footer\":[";
		//
		// double sum_v_cc = 0;
		// double sum_v_sxf = 0;
		// double sum_v_js = 0;
		// for (Record record : page.getList()) {
		// sum_v_cc += record.getDouble("v_cc");
		// sum_v_sxf += record.getDouble("v_sxf");
		// sum_v_js += record.getDouble("v_js");
		// }
		//
		// JSONObject o = new JSONObject();
		// o.put("order_id", "总计:(单位/元)");
		// o.put("v_cc", xx.toDouble(String.format("%.2f", sum_v_cc)));
		// o.put("v_sxf", xx.toDouble(String.format("%.2f", sum_v_sxf)));
		// o.put("v_js", xx.toDouble(String.format("%.2f", sum_v_js)));
		// json += o.toJSONString();
		//
		// json += "]";
		// }

		 json += "}";

		renderJson(json);
	}

	/**
	 * 新增
	 * @throws Exception 
	 */
	public void add() throws Exception {
		String objectCode = getPara(0);
		final MetaObject object = sm.meta.getMeta(objectCode);

		String json = getPara("rows");
		final List<Record> records = getRecordsByJson(json, object.getFields(), object.getPk());

		intercept = TemplateUtil.initIntercept(object.getBizIntercept());
		// 事务
		boolean flag = Db.use(object.getDs()).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					for (Record record : records) {

						AopContext ac = new AopContext(ctrl, record,object);

						// 新增前置任务
						if (intercept != null) {
							String msg = intercept.addBefore(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
						if (xx.isEmpty(object.getTable())) {
							throw new Exception("视图暂时不支持Grid 单元格编辑，请使用Form模式！");
						}
						Db.use(object.getDs()).save(object.getTable(), object.getPk(), record);
						EovaLog.dao.info(ctrl, EovaLog.ADD, object.getStr("code"));
						// 新增后置任务
						if (intercept != null) {
							String msg = intercept.addAfter(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
					}

				} catch (Exception e) {
					errorInfo = TemplateUtil.buildException(e);
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

		if (!flag) {
			renderJson(new Easy("新增失败" + errorInfo));
			return;
		}

		// 新增成功之后
		if (intercept != null) {
			try {
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

		renderJson(new Easy());
	}

	/**
	 * 删除(如果配置了 delete_field_name 则尽量删除，否则直接删除)
	 * @throws Exception 
	 */
	public void delete() throws Exception {
		
		System.out.println("GridController.delete begins...");
		if(xx.isEmpty(xx.getConfig("delete_field_name")))
			deleteOrHide(1);
		else
			deleteOrHide(3);
	}
	
	/**
	 * 隐藏
	 * @throws Exception 
	 */
	public void hide() throws Exception {
		
		System.out.println("GridController.hide begins...");
		deleteOrHide(2);
	}

	
	/**
	 * 删除或者隐藏（目前主要是系统存在隐藏，业务层暂无）
	 * @param isDel 1-删除，2-隐藏，3-尽量删除
	 * @throws Exception
	 */
	protected void deleteOrHide(final int isDel) throws Exception {
		
		System.out.println("GridController.deleteOrHide begins...");
		
		String objectCode = getPara(0);
		final MetaObject object = sm.meta.getMeta(objectCode);

		String json = getPara("rows");

		final List<Record> records = getRecordsByJson(json, object.getFields(), object.getPk());

		intercept = TemplateUtil.initIntercept(object.getBizIntercept());
		// 事务
		boolean flag = Db.use(object.getDs()).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					for (Record record : records) {

						AopContext ac = new AopContext(ctrl, record,object);

						// 删除前置任务
						if (intercept != null) {
							String msg = intercept.deleteBefore(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
						String pk = object.getPk();
						String pkValue = record.get(pk).toString();
						if (!xx.isEmpty(object.getTable())) {
							
//							if(EovaConst.DS_EOVA.equals(object.getDs())){
								if (isDel == 1) {
									// 删除数据
									Db.use(object.getDs()).delete(object.getTable(), pk, record);
								} else if(isDel == 2) {
									// 隐藏数据
									String hideFieldName = xx.getConfig("hide_field_name", "is_hide");
									String sql = String.format("update %s set %s = 1 where %s = ?", object.getTable(), hideFieldName, pk);
									Db.use(object.getDs()).update(sql, record.get(pk));
								}else if(isDel == 3){//优先使用逻辑删除，否则直接删除
									String delFieldName = xx.getConfig("delete_field_name", "is_delete");
									//检查是否存在字段，否则直接删除
									if(record.getColumns().containsKey(delFieldName)){
										String sql = String.format("update %s set %s = 1 where %s = ?", object.getTable(), delFieldName, pk);
										Db.use(object.getDs()).update(sql, record.get(pk));
									}else{
										// 删除数据
										Db.use(object.getDs()).delete(object.getTable(), pk, record);
									}
								}
							//}
							
							
						} else {
							// 视图无法自动删除，请自定义元对象业务拦截完成删除逻辑！
							// MetaObjectIntercept.deleteBefore();
							//直接业务删除了
							if (isDel == 1) {
								WidgetManager.operateView(TemplateConfig.DELETE, object, record);
							}else if (isDel == 3){
								//System.err.println("未实现视图的逻辑删除");
								
								WidgetManager.operateView(TemplateConfig.DELETE_LOGICAL, object, record);
							}else{
								System.err.println("暂时不支持视图隐藏");
							}
						}
						EovaLog.dao.info(ctrl, EovaLog.DELETE, object.getStr("code") + "[" + pkValue + "]");
						// 删除后置任务
						if (intercept != null) {
							String msg = intercept.deleteAfter(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
					}
				} catch (Exception e) {
					errorInfo = "删除异常:" + TemplateUtil.buildException(e);
					return false;
				}
				return true;
			}
		});

		if (!flag) {
			renderJson(new Easy(errorInfo));
			return;
		}

		// 删除成功之后
		if (intercept != null) {
			try {
				AopContext ac = new AopContext(this, records,object);
				String msg = intercept.deleteSucceed(ac);
				if (!xx.isEmpty(msg)) {
					errorInfo = msg;
				}
			} catch (Exception e) {
				errorInfo = TemplateUtil.buildException(e);
				renderJson(new Easy("删除成功,deleteSucceed执行异常!" + errorInfo));
				return;
			}
		}

		if (!xx.isEmpty(errorInfo)) {
			renderJson(new Easy(errorInfo));
			return;
		}

		renderJson(new Easy());
	}

	/**
	 * 更新
	 * @throws Exception 
	 */
	public void update() throws Exception {

		String objectCode = getPara(0);
		final MetaObject object = sm.meta.getMeta(objectCode);

		String json = getPara("rows");

		final List<Record> records = getRecordsByJson(json, object.getFields(), object.getPk());

		intercept = TemplateUtil.initIntercept(object.getBizIntercept());
		// 事务
		boolean flag = Db.use(object.getDs()).tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				try {
					for (Record record : records) {

						AopContext ac = new AopContext(ctrl, record,object);

						// 修改前置任务
						if (intercept != null) {
							String msg = intercept.updateBefore(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
						if (xx.isEmpty(object.getTable())) {
							throw new Exception("视图暂时不支持Grid单元格编辑，请使用Form模式！");
						}
						Db.use(object.getDs()).update(object.getTable(), object.getPk(), record);
						EovaLog.dao.info(ctrl, EovaLog.UPDATE, object.getStr("code") + "[" + record.get(object.getPk()) + "]");
						// 修改后置任务
						if (intercept != null) {
							String msg = intercept.updateAfter(ac);
							if (!xx.isEmpty(msg)) {
								errorInfo = msg;
								return false;
							}
						}
					}

				} catch (Exception e) {
					errorInfo = TemplateUtil.buildException(e);
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

		if (!flag) {
			renderJson(new Easy("修改失败" + errorInfo));
			return;
		}

		// 修改成功之后
		if (intercept != null) {
			try {
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

		renderJson(new Easy());
	}

	/**
	 * json转List
	 * 
	 * @param json
	 * @param pkName
	 * @return
	 */
	private static List<Record> getRecordsByJson(String json, List<MetaField> items, String pkName) {
		List<Record> records = new ArrayList<Record>();

		List<JSONObject> list = JSON.parseArray(json, JSONObject.class);
		for (JSONObject o : list) {
			Map<String, Object> map = JSON.parseObject(o + "", new TypeReference<Map<String, Object>>() {
			});
			Record re = new Record();
			re.setColumns(map);
			// 将Text翻译成Value,然后删除val字段
			for (MetaField x : items) {
				String en = x.getEn();// 字段名
				String exp = x.getStr("exp");// 表达式
				Object value = re.get(en);// 值
				//if(xx.isEmpty(value)){
					if (!xx.isEmpty(exp)) {
						String valField = en + "_val";
						// 获取值列中的值
						value = re.get(valField);
						
						// 获得值之后删除值列防止持久化报错
						re.remove(valField);
					}
				//}
				
				if(!xx.isEmpty(value))
					re.set(en, TemplateUtil.convertValue(x, value,0));
			}
			// 删除主键备份值列
			re.remove("pk_val");
			// 删除Orcle分页产生的rownum_
			if (xx.isOracle()) {
				re.remove("rownum_");
			}
			records.add(re);
		}

		return records;
	}

	public static void main(String[] args) {

		String sl = "[{'id':1,'loginId':'111'},{'id':2,'loginId':'222'}]";
		List<JSONObject> list = JSON.parseArray(sl, JSONObject.class);
		for (JSONObject o : list) {
			Map<String, Object> map = JSON.parseObject(o + "", new TypeReference<Map<String, Object>>() {
			});
			Record re = new Record();
			re.setColumns(map);
			System.out.println(re.toJson());
		}
	}

}