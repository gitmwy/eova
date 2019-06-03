package com.eova.template.org;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.Response;
import com.eova.common.utils.xx;
import com.eova.config.EovaConst;
import com.eova.config.PageConst;
import com.eova.core.menu.config.MenuConfig;
import com.eova.core.menu.config.TreeConfig;
import com.eova.model.Button;
import com.eova.model.Menu;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.model.User;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.eova.widget.WidgetManager;
import com.eova.widget.WidgetUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * Org组件(类树，但是响应数据结构不一致)
 *
 * @author jzhao
 *
 */
public class OrgController extends Controller {

	final Controller ctrl = this;
	
	public void list() {

		String menuCode = this.getPara(0);

		// 获取元数据
		Menu menu = Menu.dao.findByCode(menuCode);
		MenuConfig config = menu.getConfig();
		String objectCode = config.getObjectCode();
		MetaObject object = MetaObject.dao.getByCode(objectCode);
//		List<MetaField> fields = MetaField.dao.queryByObjectCode(objectCode);

		// 根据权限获取功能按钮
		User user = this.getSessionAttr(EovaConst.USER);
		List<Button> btnList = Button.dao.queryByMenuCode(menuCode, user);

		// 是否需要显示快速查询
		setAttr("isQuery", MetaObject.dao.isExistQuery(objectCode));

		setAttr("menu", menu);
		setAttr("btnList", btnList);
		setAttr("object", object);
//		setAttr("fields", fields);
		
		
		
		formPageType("imglist");
		String template = this.getPara(PageConst.Template);//模板
		if (!xx.isEmpty(template)) 
			render("/eova/template/org/list_"+template+".html");
		else
			render("/eova/template/org/list.html");
		
	}
	
	/** 元对象业务拦截器 **/
	protected MetaObjectIntercept intercept = null;

	@SuppressWarnings("unused")
	public void query() throws Exception {

		String code = getPara(0);
		String menuCode = getPara(1);

		MetaObject object = sm.meta.getMeta(code);
		List<MetaField> fields = MetaField.dao.queryByObjectCode(code);
		Menu menu = null;
		if (!xx.isEmpty(menuCode))
			menu = Menu.dao.findByCode(menuCode);

		String filter = object.getStr("filter");
		// 菜单初始过滤条件优先级高于对象初始过滤条件
		if (menu != null) {
			String menuFilter = menu.getStr("filter");
			if (!xx.isEmpty(menuFilter))
				filter = menuFilter;
		}

		List<Object> parmList = new ArrayList<Object>();

		// 获取条件
		String where = WidgetManager.getWhere(this, fields, parmList, filter);
		// 获取排序
		String sort = WidgetManager.getSort(this);

		// 分页查询Grid数据
		String view = object.getView();
		String sql = "from " + view + where + sort;

		intercept = TemplateUtil.initIntercept(object.getBizIntercept());

		// 查询前置任务
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl);
			intercept.queryBefore(ac);

			// AOP自定义条件和参数
			if (!xx.isEmpty(ac.condition)) {
				sql = String.format("from %s where %s %s", view, ac.condition, sort);
				parmList = ac.params;
			}
		}

		// 转换SQL参数
		Object[] paras = new Object[parmList.size()];
		parmList.toArray(paras);

		// 默认查询所有字段(如果想显示图标字段必须叫iconskip)
		String selelct = "select *";
		// 如果存在配置仅查询配置项
		if (menu != null) {
			// 根据Tree配置构造查询项
			TreeConfig tc = menu.getConfig().getTree();
//			selelct = MessageFormat.format("select {0},{1},{2},{3}", tc.getObjectField(), tc.getIdField(), tc.getParentField(), tc.getTreeField());
			selelct = MessageFormat.format("select {0},{1},{2}",  tc.getIdField(), tc.getParentField(), tc.getTreeField());
			if (!xx.isEmpty(tc.getIconField())) {
				selelct += ',' + tc.getIconField();
			}
		}
		List<Record> list = Db.use(object.getDs()).find(selelct + " " + sql, paras);

		// 查询后置任务
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl, list,object);
			intercept.queryAfter(ac);
		}
		
		// 备份Value列，然后将值列转换成Key列
		WidgetUtil.copyValueColumn(list, object.getPk(), object.getFields());
		// 根据表达式将数据中的值翻译成汉字
		WidgetManager.convertValueByExp(this, object.getFields(), list);
		
		// 获取登录用户的角色
		User user = getSessionAttr(EovaConst.USER);
//		int rid = user.getRid();
		
//		{    'id':'123'
//	          'name': 'Lao Lao',
//	          'title': 'general manager',
//	          'children': [
//	            { 'name': 'Bo Miao', 'title': 'department manager' },
//	            { 'name': 'Su Miao', 'title': 'department manager',
//	              'children': [
//	                { 'name': 'Tie Hua', 'title': 'senior engineer' },
//	                { 'name': 'Hei Hei', 'title': 'senior engineer',
//	                  'children': [
//	                    { 'name': 'Pang Pang', 'title': 'engineer' },
//	                    { 'name': 'Xiang Xiang', 'title': 'UE engineer' }
//	                  ]
//	                }
//	              ]
//	            },
//	            { 'name': 'Yu Jie', 'title': 'department manager' },
//	            { 'name': 'Yu Li', 'title': 'department manager' },
//	            { 'name': 'Hong Miao', 'title': 'department manager' },
//	            { 'name': 'Yu Wei', 'title': 'department manager' },
//	            { 'name': 'Chun Miao', 'title': 'department manager' },
//	            { 'name': 'Yu Tie', 'title': 'department manager' }
//	          ]
//	        }
//	      }
		
		//代码调整了，后端转结构改成 前段转结构
		Response r = new Response(list);
		r.setCount(list.size());
		
//		List<Record> to = new ArrayList();
//		toTreeList(list,to,menu.getConfig().getTree());
	
		renderJson(r);
	}
	
	/**
	 * 指定自身方法或者叫类型
	 * @param fun
	 */
	protected void formPageType(String fun){
		String page = this.getPara("page");
		if(!xx.isEmpty(page)){
			setAttr("PAGE_TYPE", page);
		}else{
			setAttr("PAGE_TYPE", fun);
		}	
	}
	
	/**
	 * 转换list结果至 父子结构
	 * @param src 需要pid 倒叙排下（不然会错，是不是要这边自己排序下,目前已经排序）
	 * @param to
	 */
	public void toTreeList(List<Record> src,List<Record> to,TreeConfig tc){
		final String  pField = tc.getParentField();
		//自定义Comparator对象，自定义排序
		Comparator c = new Comparator<Record>() {
			@Override
			public int compare(Record o1, Record o2) {
				// TODO Auto-generated method stub
				if((int)o1.getInt(pField)<(int)o2.getInt(pField))
					return 1;
				//注意！！返回值必须是一对相反数，否则无效。jdk1.7以后就是这样。
		//		else return 0; //无效
				else return -1;
			}
		};
		
		Collections.sort(src, c);
		
		

		
		Record last = null;
		int lastPid = -1;
		
	
		Iterator<Record> it = src.iterator();
		
		Record lastP = null;
		while(it.hasNext()){
			Record one = it.next();
			
			//有上级节点则放入上级，本身删除
			Integer pid = one.getInt(tc.getParentField());
			
			
		    if(pid != null && pid.intValue() != 0){
		        
		        
		    	Record tempP = getPRecord(pid,src,tc,lastP);
		    	if(tempP != null){
		    		((List)tempP.get(tc.getChildrenField())).add(one);
		    	}
		    	
		        it.remove();
		        
		        lastP = tempP;
		    }else{//根节点
		    	
		    	lastP = null;
		    }
		}
	}
	
	private Record getPRecord(Integer pid ,List<Record> list,TreeConfig tc,Record lastP){
		Record p = null;
		//休息检查最后值
		if(lastP != null && lastP.getInt(tc.getIdField()).intValue() == pid.intValue()){
			p = lastP;
		}else{
			
			for(Record pone:list){
	    		Integer id =  pone.getInt(tc.getIdField());
	    		if(id.intValue() == pid){
	    			p = pone;
	    			break;
	    		}
	        }
		}
		
		
		if(p!= null && p.get(tc.getChildrenField()) == null){
			p.set(tc.getChildrenField(), new LinkedList());
		}
		return p;
	}
	
}