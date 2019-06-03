/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.widget.tree;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.Response;
import com.eova.common.utils.xx;
import com.eova.config.EovaConst;
import com.eova.core.menu.config.TreeConfig;
import com.eova.model.Menu;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.model.User;
import com.eova.template.common.util.TemplateUtil;
import com.eova.widget.WidgetManager;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * Tree组件
 *
 * @author Jieven
 *
 */
public class TreeController extends Controller {

	final Controller ctrl = this;

	/** 元对象业务拦截器 **/
	protected MetaObjectIntercept intercept = null;

	@SuppressWarnings("unused")
	public void query() throws Exception {

		String code = getPara(0);
		String menuCode = getPara(1);

		MetaObject object = MetaObject.dao.getByCode(code);
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
			selelct = MessageFormat.format("select {0},{1},{2},{3}", tc.getObjectField(), tc.getIdField(), tc.getParentField(), tc.getTreeField());
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

		// 获取登录用户的角色
//		User user = getSessionAttr(EovaConst.USER);
//		int rid = user.getRid();

		// 根据角色获取已授权菜单Code
		// List<String> authMenuCodes = Button.dao.queryMenuCodeByRid(rid);
		//
		// // 获取已授权菜单
		// LinkedHashMap<Integer, Menu> authMenu = new LinkedHashMap<Integer, Menu>();
		// for (Map.Entry<Integer, Menu> map : allMenu.entrySet()) {
		// Menu menu = map.getValue();
		// // TODO 仅可查看已授权部分
		// if (authMenuCodeList.contains(menu.getStr("code"))) {
		// authMenu.put(map.getKey(), menu);
		// }
		// }

		// Map 转 Tree Json
		// String json = WidgetUtil.menu2TreeJson(authParent, rootId);

		// .replaceAll("iconskin", "iconSkin")
		//renderJson(JsonKit.toJson(list));
		
		renderJson(Response.sucData(list) );
	}

}