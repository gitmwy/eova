/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.core.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.eova.cloud.EovaCloud;
import com.eova.common.Easy;
import com.eova.common.base.BaseCache;
import com.eova.common.utils.xx;
import com.eova.common.utils.db.DbUtil;
import com.eova.config.EovaConfig;
import com.eova.config.EovaConst;
import com.eova.core.button.ButtonFactory;
import com.eova.core.menu.config.ChartConfig;
import com.eova.core.menu.config.MenuConfig;
import com.eova.core.menu.config.TreeConfig;
import com.eova.model.Button;
import com.eova.model.Menu;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.model.RoleBtn;
import com.eova.template.common.config.TemplateConfig;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.NestedTransactionHelpException;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.activerecord.tx.TxConfig;

/**
 * 菜单管理
 *
 * @author Jieven
 * @date 2014-9-11
 */
public class MenuController extends Controller {

	public void toAdd() {
		keepPara("parent_id");
		render("/eova/menu/form.html");
	}

	public void toUpdate() {
		int pkValue = getParaToInt(1);
		Menu menu = Menu.dao.findById(pkValue);

		setAttr("menu", menu);

		render("/eova/menu/form.html");
	}

	/**
	 * 菜单基本功能管理
	 */
	public void toMenuFun() {
		String menuCode = getPara(0);
		Menu menu = Menu.dao.findByCode(menuCode);

		setAttr("menu", menu);

		HashMap<Integer, List<Button>> btnMap = new HashMap<Integer, List<Button>>();

		List<Button> btns = Button.dao.findNoQueryByMenuCode(menuCode);
		for (Button b : btns) {
			int group = b.getInt("group_num");
			List<Button> list = btnMap.get(group);
			if (list == null) {
				list = new ArrayList<Button>();
				btnMap.put(group, list);
			}
			list.add(b);
		}

		setAttr("btnMap", btnMap);

		render("/eova/menu/menuFun.html");
	}

	// 一键导入
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void addAll() {
		if (!getPara(0, "").equals("eova")) {
			renderJson(new Easy("请输入校验码，防止误操作！！！！！"));
			return;
		}

		List<MetaObject> objects = MetaObject.dao.find("select * from eova_object where id >= 1100");
		for (MetaObject o : objects) {

			String menuCode = o.getStr("code");
			System.out.println("create " + menuCode);
			Menu menu = new Menu();
			menu.set("parent_id", 3);
			menu.set("name", o.getStr("name"));
			menu.set("code", menuCode);
			menu.set("type", TemplateConfig.SINGLE_GRID);

			// 菜单配置
			MenuConfig config = new MenuConfig();
			config.setObjectCode(o.getStr("code"));
			menu.setConfig(config);
			menu.save();

//			createMenuButton(menuCode, TemplateConfig.SINGLE_GRID, config);
			// 创建菜单按钮
			new ButtonFactory(TemplateConfig.SINGLE_GRID).build(menuCode, config);

			// 还原成默认状态
			o.set("diy_card", null);
			o.update();
		}
		// 新增菜单使缓存失效
		BaseCache.delSer(EovaConst.ALL_MENU);
		
		renderJson(new Easy("Auto Create Menu:" + objects.size(), true));
	}

	// 导出选中菜单数据
	public void doExport() {
		String ids = getPara(0);

		StringBuilder sb = new StringBuilder();

		String sql = "select * from eova_menu where id in (" + ids + ")";
		List<Record> objects = Db.use(xx.DS_EOVA).find(sql);
		DbUtil.generateSql(objects, "eova_menu", "id", sb);

		renderText(sb.toString());
	}

	/**
	 * 新增菜单
	 */
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void add() {

		String menuCode = getPara("code");
		String type = getPara("type");

		String sql = "select * from eova_menu where code = ?";
		Menu temp = Menu.dao.findFirst(sql, menuCode);
		if (temp != null) {
			renderJson(new Easy("菜单编码不能重复"));
			return;
		}
		String icon = getPara("iconField");
		if (!xx.isEmpty(icon) && icon.equalsIgnoreCase("icon")) {
			renderJson(new Easy("Tree图标字段:字段名不能为icon(系统关键字，你可以改为：iconskip)"));
			return;
		}
		icon = getPara("treeGridIconField");
		if (!xx.isEmpty(icon) && icon.equalsIgnoreCase("icon")) {
			renderJson(new Easy("Tree图标字段:字段名不能为icon(系统关键字，你可以改为：iconskip)"));
			return;
		}
		
		try {
			
			Menu menu = new Menu();
			menu.set("parent_id", getPara("parent_id"));
			menu.set("iconskip", getPara("icon", ""));
			menu.set("name", getPara("name"));
			menu.set("code", menuCode);
			menu.set("order_num", getPara("indexNum"));
			menu.set("type", type);
			// menu.set("biz_intercept", getPara("bizIntercept", ""));
			menu.set("url", getPara("url", ""));

			// 菜单配置
			MenuConfig config = new MenuConfig();
			// 构建菜单配置项
			buildConfig(type, config);
			menu.setConfig(config);
			menu.save();

			// 目录没有默认按钮
			if (type.equals(Menu.TYPE_DIR)) {
				renderJson(new Easy());
				return;
			}

			// 创建菜单按钮
			new ButtonFactory(type).build(menuCode, config);
			
			// 新增菜单使缓存失效
			BaseCache.delSer(EovaConst.ALL_MENU);
			
			EovaCloud.app();
			
			renderJson(new Easy());
		} catch (Exception e) {
			e.printStackTrace();
			renderJson(new Easy("新增菜单失败,请仔细查看控制台日志！"));
			throw new NestedTransactionHelpException("新增菜单异常");
		}
		
	}

	/**
	 * 配置菜单
	 *
	 * @param type 模版类型
	 * @param config
	 */
	private void buildConfig(String type, MenuConfig config) {
		if (type.equals(TemplateConfig.SINGLE_GRID)) {
			// 单表
			config.setObjectCode(getPara("objectCode"));
		} else if (type.equals(TemplateConfig.SINGLE_TREE)) {
			// 单表树
			config.setObjectCode(getPara("singleTreeObjectCode"));

			TreeConfig tc = new TreeConfig();
			tc.setIconField(getPara("iconField"));
			tc.setTreeField(getPara("treeField"));
			tc.setParentField(getPara("parentField"));
			tc.setIdField(getPara("idField", "id"));
			tc.setRootPid(getPara("rootPid"));
			config.setTree(tc);
		} else if (type.equals(TemplateConfig.SINGLE_CHART)) {
			// 单表图
			config.setObjectCode(getPara("singleChartObjectCode"));

			String ys = getPara("singleChartY");
			List<String> ens = Arrays.asList(ys.split(","));

			// 根据字段英文名，获取字段中文名
			List<String> ycn = new ArrayList<>();
			List<MetaField> fields = MetaField.dao.queryFields(config.getObjectCode());
			for (String en : ens) {
				for (MetaField f : fields) {
					if (f.getEn().equals(en)) {
						ycn.add(f.getCn());
						break;
					}
				}
			}

			ChartConfig cc = new ChartConfig();
			cc.setX(getPara("singleChartX"));
			cc.setYunit(getPara("singleChartYunit"));
			cc.setY(ens);
			cc.setYcn(ycn);
			config.setChart(cc);

		} else if (type.equals(TemplateConfig.MASTER_SLAVE_GRID)) {
			// 主
			String masterObjectCode = getPara("masterObjectCode");
			String masterFieldCode = getPara("masterFieldCode");
			config.setObjectCode(masterObjectCode);
			config.setObjectField(masterFieldCode);

			// 子
			ArrayList<String> objects = new ArrayList<String>();
			ArrayList<String> fields = new ArrayList<String>();
			for (int i = 1; i <= 5; i++) {
				String slaveObjectCode = getPara("slaveObjectCode" + i);
				String slaveFieldCode = getPara("slaveFieldCode" + i);
				if (xx.isOneEmpty(slaveObjectCode, slaveFieldCode)) {
					break;
				}
				objects.add(slaveObjectCode);
				fields.add(slaveFieldCode);
			}
			config.setObjects(objects);
			config.setFields(fields);
		} else if (type.equals(TemplateConfig.TREE_GRID)) {
			// 树&表
			TreeConfig tc = new TreeConfig();
			tc.setObjectCode(getPara("treeGridTreeObjectCode"));
			tc.setObjectField(getPara("treeGridTreeFieldCode"));

			tc.setIconField(getPara("treeGridIconField"));
			tc.setTreeField(getPara("treeGridTreeField"));
			tc.setParentField(getPara("treeGridParentField"));
			tc.setIdField(getPara("treeGridIdField", "id"));
			tc.setRootPid(getPara("treeGridRootPid"));
			config.setTree(tc);

			config.setObjectCode(getPara("treeGridObjectCode"));
			config.setObjectField(getPara("treeGridFieldCode"));
		}else if (type.equals(TemplateConfig.IMG_GRID)) {
			// 单表
			config.setObjectCode(getPara("imgObjectCode"));
		}else if(type.equals(TemplateConfig.ORG_GRID)){
			//组织
			TreeConfig tc = new TreeConfig();
			

			tc.setIconField(getPara("orgIconField"));
			tc.setTreeField(getPara("orgGridField"));
			tc.setParentField(getPara("orgParentField"));
			tc.setIdField(getPara("orgIdField", "id"));
			//tc.setRootPid(getPara("treeGridRootPid"));
			config.setTree(tc);

			config.setObjectCode(getPara("orgGridObjectCode"));
			
		}
		
	}

	/**
	 * 菜单功能管理
	 */
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void menuFun() {
		String menuCode = getPara(0);

		List<Button> btns = Button.dao.findFunByMenuCode(menuCode);
		// 动态获取按钮是否禁用
		for (Button btn : btns) {
			Integer groupNum = btn.getInt("group_num");
			Integer id = btn.getInt("id");
			// 获取按钮选择状态，勾选为启用，反选为禁用
			boolean isDel = getParaToBoolean(groupNum + "_" + id, true);
			// 按钮有变更
			if (btn.getBoolean("is_hide") != isDel) {
				btn.set("is_hide", isDel);
				btn.update();
			}
		}

		renderJson(new Easy());
	}
	
	// eova_button 升级 V1.4/1.5 -> V1.6
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void v16ButtonUpdate() {
		
		boolean isUpgrade = xx.getConfigBool("isUpgrade", false);
		if (!isUpgrade) {
			renderText("未开启升级模式，请启动配置 isUpgrade = true");
			return;
		}
		
		String sql = "select distinct(menu_code) from eova_button";
		List<String> codes = Db.use(xx.DS_EOVA).query(sql);
		
		// 修正异常的 查询按钮
		int num = Db.use(xx.DS_EOVA).update("update eova_button set is_base = 1 where ui = 'query'");
		System.err.println("修复异常的按钮数=" + num);
		// 删除所有查询按钮和基础功能
		num = Db.use(xx.DS_EOVA).update("delete from eova_button where is_base = 1 or ui = 'query'");
		System.err.println("删除按钮数=" + num);
		
		for (String code : codes) {
			Menu menu = Menu.dao.findByCode(code);
			if (menu.getInt("type").equals(Menu.TYPE_DIR)) {
				continue;
			}
			new ButtonFactory(menu.getStr("type")).build(code, menu.getConfig());
		}
		
		// 删除无效权限数据
		num = Db.use(xx.DS_EOVA).update("delete from eova_role_btn");
		System.err.println("权限数据被清空");
		
		// 初始化超管权限
		sql = "select id from eova_button where menu_code = ?";
		List<Integer> ids = Db.use(xx.DS_EOVA).query(sql, "sys_auth_role");
		for (Integer id : ids) {
			RoleBtn rb = new RoleBtn();
			rb.set("bid", id);
			rb.set("rid", EovaConst.ADMIN_RID);
			rb.save();
		}
		
		initNewMenu();
		
		// 初始化EOVA按钮
		initEovaButton();
		
		
		renderText(" V1.4/1.5 -> V1.6 升级成功,请重新对所有角色重新进行权限分配！");
	}

	/**
	 * 初始化新菜单(没有生成按钮的菜单)
	 */
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void initNewMenu() {

		String isUpgrade = EovaConfig.getProps().get("isUpgrade");
		if (xx.isEmpty(isUpgrade) || !isUpgrade.equals("true")) {
			renderText("未开启升级模式，请启动配置 eova.config isUpgrade = true");
			return;
		}

		String sql = "select code from eova_menu where type not in ('dir') and code not in (select DISTINCT(menu_code) from eova_button);";
		List<String> codes = Db.use(xx.DS_EOVA).query(sql);

		for (String code : codes) {
			System.out.println(code);
			Menu menu = Menu.dao.findByCode(code);
			new ButtonFactory(menu.getStr("type")).build(code, menu.getConfig());
		}
		
		System.err.println("自动修复未生成按钮的菜单成功");
		
		renderText("自动为没有按钮的菜单初始化成功！");
	}
	
	/**
	 * 初始化指定菜单的基础按钮
	 */
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void initMenuButton() {

		String isUpgrade = EovaConfig.getProps().get("isUpgrade");
		if (xx.isEmpty(isUpgrade) || !isUpgrade.equals("true")) {
			renderText("未开启升级模式，请启动配置 eova.config isUpgrade = true");
			return;
		}
		
		String menuCode = getPara(0);
		if (menuCode == null) {
			renderText("请输入指定菜单编码");
			return;
		}
		// 删除所有基础按钮
		Db.use(xx.DS_EOVA).update("delete from eova_button where is_base = 1 and menu_code = ?", menuCode);
		Menu menu = Menu.dao.findByCode(menuCode);
		new ButtonFactory(menu.getStr("type")).build(menuCode, menu.getConfig());
		
		System.err.println("初始化菜单按钮成功：" + menuCode);
		
		renderText("初始化菜单按钮成功！");
	}
	
	/**
	 * 初始化EOVA按钮信息
	 */
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void initEovaButton() {

		String isUpgrade = EovaConfig.getProps().get("isUpgrade");
		if (xx.isEmpty(isUpgrade) || !isUpgrade.equals("true")) {
			renderText("未开启升级模式，请启动配置 eova.config isUpgrade = true");
			return;
		}

		{
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and name = ?";
			Db.use(xx.DS_EOVA).update(sql, "eova_menu", "新增");
		}
		{
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and name = ?";
			Db.use(xx.DS_EOVA).update(sql, "eova_menu", "查看");
		}
		{
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and name = ?";
			Db.use(xx.DS_EOVA).update(sql, "eova_task", "导入");
		}
		{
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and name = ?";
			Db.use(xx.DS_EOVA).update(sql, "eova_object", "新增");
		}
		{
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and name = ?";
			Db.use(xx.DS_EOVA).update(sql, "eova_object", "查看");
		}
		{
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and name = ?";
			Db.use(xx.DS_EOVA).update(sql, "eova_object", "新增");
		}
		{
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and name = ?";
			Db.use(xx.DS_EOVA).update(sql, "sys_auth_users", "查看");
		}
		{
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and name = ?";
			Db.use(xx.DS_EOVA).update(sql, "sys_auth_users", "用户详细信息新增");
		}
		{
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and name = ?";
			Db.use(xx.DS_EOVA).update(sql, "sys_auth_users", "用户详细信息删除");
		}
		{	
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and name = ?";
			Db.use(xx.DS_EOVA).update(sql, "sys_auth_role", "查看");
		}
		{	
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and name = ?";
			Db.use(xx.DS_EOVA).update(sql, "sys_auth_role", "导入");
		}
		{	
			String sql = "UPDATE eova_button SET is_hide = 1 WHERE menu_code = ? and ui <> 'query'";
			Db.use(xx.DS_EOVA).update(sql, "sys_log");
		}
		
		System.err.println("初始化EOVA按钮信息成功！");

		renderText("初始化EOVA按钮信息成功！");
	}

}