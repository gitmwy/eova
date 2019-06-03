/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.core;

import com.eova.cloud.AuthCloud;
import com.eova.common.Easy;
import com.eova.common.utils.EncryptUtil;
import com.eova.common.utils.db.DbUtil;
import com.eova.common.utils.xx;
import com.eova.config.EovaConfig;
import com.eova.config.EovaConst;
import com.eova.model.Button;
import com.eova.model.EovaLog;
import com.eova.model.Menu;
import com.eova.model.User;
import com.eova.service.sm;
import com.eova.widget.WidgetUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.CaptchaRender;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Eova入口
 *
 * @author Jieven
 * @date 2015-1-6
 */
public class IndexController extends Controller {

	public void captcha() {
		render(new CaptchaRender());
	}

	public void code() {
		setAttr("exp1", "select id UID,login_id CN from users where <%if(user.id != 0){%>  id > ${user.id}<%}%> order by id desc");
		render("/eova/code.html");
	}

	public void toIndex() {
		render("/eova/index.html");
	}

	public void header() {
		User user = getSessionAttr(EovaConst.USER);

		setAttr("user", user);

		render("/eova/header.html");
	}

	public void footer() {
		render("/eova/footer.html");
	}

	public void toIcon() {
		
		render("/eova/icon_lay.html");
	}

	public void toUe() {
		render("/eova/uedemo.html");
	}

	public void toTest() {
		setAttr("id", "testGrid");
		setAttr("objectCode", getPara(0));
		render("/eova/test.html");
	}

	public void toForm() {
		render("/eova/test/form.html");
	}

	public void toLogin() {
		boolean isCaptcha = xx.toBoolean(EovaConfig.getProps().get("isCaptcha"), false);
		
		setAttr("isCaptcha", isCaptcha);
		
		//render("/eova/login.html");
		//DEV,测试环境=TEST
		String env = EovaConfig.getProps().get("env");
		if(("DEV".equalsIgnoreCase(env) || "TEST".equalsIgnoreCase(env) )
				&& this.getAttr("loginId") == null){
			
			setAttr("loginId", "eova");
			setAttr("loginPwd", "000000");
			render("/eova/login.html");
			
			
		}else{
			
			//setAttr("isCaptcha", true);
			render("/eova/login.html");
		}
//		render("/eova/login.html");
	}

	

	public void index() {

		// 请勿屏蔽此处内容，可能会导致您违反契约成为非法用户，被社区所遗弃！
		String appId = xx.getConfig("app_id");
		String appSecret = xx.getConfig("app_secret");
		if (xx.isEmpty(appId)) {
			renderHtml("启动之前请先配置应用信息：<br>配置文件：/src/main/resources/default/app.config<br>小贴士：应用ID和应用密钥在【Eova应用中心】<br>立即<a href=\"http://www.eova.cn/app\">免费注册获取</a>");
			return;
		}
		if (!AuthCloud.isAuthApp(appId, appSecret)) {
			renderHtml("您使用了一个无效的应用ID或应用密钥，请仔细检查！<br>应用ID和应用密钥在【Eova社区>应用中心】<br>立即<a href=\"http://www.eova.cn/app\">免费注册获取</a>");
			return;
		}
		// 请勿屏蔽此处内容，可能会导致您违反契约成为非法用户，被社区所遗弃！

		/*
		 * Eova契约简介 普通用户请保留底部版权声明，如需去掉版权，请捐赠成为VIP用户，支持开源项目发展，没有用户支持的项目最终只能搁浅，最后受损失的是所有Eova用户！ 所有用户请在社区进行应用认证，并且获取正确AppId和AppSecret进行配置！AppId使用场景有：VIP高级功能，Eova社区问答，Eova云服务...
		 * 如违反本契约的用户，当被检测到或被举报时，会被社区标识为非法用户，将会失去一切社区的支持和服务！一次违约，永不解禁！ 再次呼吁大家遵守契约，细水长流，和谐发展！
		 */

		User user = getSessionAttr(EovaConst.USER);
		// 已经登录
		if (user != null) {
			// 默认主页
			setAttr("URL_MAIN", xx.getConfig("app_main"));
			setAttr("user", user);
			
			setAttr("topMenu", getTopMenu());
			
			toIndex();
			return;
		}

		// 未登录
		toLogin();
	}

	public void doExit() {
		// 清除登录状态
		removeSessionAttr(EovaConst.USER);
		redirect("/");
	}

	public void doLogin() {
		String loginId = getPara("loginId");
		String loginPwd = getPara("loginPwd");
		setAttr("loginId", loginId);
		
		boolean isCaptcha = xx.toBoolean(EovaConfig.getProps().get("isCaptcha"), false);
		if (isCaptcha && !super.validateCaptcha("captcha")) {
			setAttr("msg", "验证码错误，请重新输入！");
			toLogin();
			
			EovaLog.dao.info(this, EovaLog.LOGIN, loginId+":"+ getAttr("msg"));
			return;
		}
		
		
		
		
		User user = User.dao.getByLoginId(loginId);
		if (user == null) {
			setAttr("msg", "用户名不存在");
			toLogin();
			return;
		}
		
		//超管在生产模式禁用
		
		
		String env = EovaConfig.getProps().get("env");
		if(user.isAdmin()){
//			if("PRE".equalsIgnoreCase(env)  ){
//			
//				setAttr("msg", "生产环境非法用户");
//				toLogin();
//				return;
//			
//			}
			if("PRD".equalsIgnoreCase(env)){
				setAttr("msg", "用户名不存在");
				toLogin();
				EovaLog.dao.info(this, EovaLog.LOGIN, loginId+":"+ getAttr("msg"));
				return;
			}
		}
		
//		if("PRE".equalsIgnoreCase(env) || "PRD".equalsIgnoreCase(env)){
//			if(user.getRid() == admin_rid){
//				setAttr("msg", "生产环境非法用户");
//				toLogin();
//				return;
//			}
//		}
		
		
		if (!user.getStr("login_pwd").equals(EncryptUtil.getSM32(loginPwd))) {
			setAttr("msg", "密码错误");
			keepPara("loginId");
			toLogin();
			EovaLog.dao.info(this, EovaLog.LOGIN, loginId+":"+ getAttr("msg"));
			return;
		}

		

		try {
			loginInit(this, user);
		} catch (Exception e) {
			e.printStackTrace();
			setAttr("msg", e.getMessage());
			keepPara("loginId");
			toLogin();
			EovaLog.dao.info(this, EovaLog.LOGIN, loginId+":"+ getAttr("msg"));
			return;
		}
		
		user.init();
		
		setSessionAttr(EovaConst.USER, user);
		EovaLog.dao.info(this, EovaLog.LOGIN, "成功");
		// 重定向到首页
		redirect("/");
	}

	/**
	 * 初始权限（根据角色）
	 *
	 * @param user
	 * @throws Exception
	 */
	protected void loginInit(Controller ctrl, User user) throws Exception {
		// 初始化获取授权信息
		
		Set<String> auths = new HashSet<String>();
		String sql = "SELECT bs FROM eova_role_btn rf LEFT JOIN eova_button b ON rf.bid = b.id WHERE rf.rid in "+DbUtil.joinIds(user.getRids());
		List<Record> bss = Db.use(xx.DS_EOVA).find(sql);
		for (Record r : bss) {
			String bs = r.getStr("bs");
			if (xx.isEmpty(bs)) {
				continue;
			}
			if (!bs.contains(";")) {
				auths.add(bs);
				continue;
			}
			String[] strs = bs.split(";");
			for (String str : strs) {
				auths.add(str);
			}
		}
		if (xx.isEmpty(auths)) {
			throw new Exception("用户角色没有任何授权,请联系管理员授权");
		}
		user.put("auths", auths);

		// 子类可重写添加业务属性和对象
	}

	/**
	 * 修改密码
	 */
	public void updatePwd() {
		String oldPwd = getPara("oldPwd");
		String newPwd = getPara("newPwd");
		String confirm = getPara("confirmPwd");

		if (xx.isOneEmpty(oldPwd, newPwd, confirm)) {
			renderJson(new Easy("三个密码都不能为空"));
			return;
		}

		// 新密码和确认密码是否一致
		if (!newPwd.equals(confirm)) {
			renderJson(new Easy("新密码两次输入不一致"));
			return;
		}

		// 当前用户
		User user = getSessionAttr(EovaConst.USER);
		String pwd = user.getStr("login_pwd");
		// 旧密码是否正确
		if (!pwd.equals(EncryptUtil.getSM32(oldPwd))) {
			renderJson(new Easy("密码错误"));
			return;
		}

		// 修改密码
		user.set("login_pwd", EncryptUtil.getSM32(newPwd)).update();

		renderJson(new Easy());
	}
	
	/**
	 * 修改密码
	 */
	public void toUpdatePwd() {
		User user = getSessionAttr(EovaConst.USER);
		if (xx.isEmpty(user)) {
			setAttr("msg", "请先登录");
			toLogin();
			return;
		}
		this.setAttr("user", user);
		render("/eova/user/changePwd.html");
	}
	
	/**
	 * 修改信息
	 */
	public void toUpdateInfo() {
		User user = getSessionAttr(EovaConst.USER);
		if (xx.isEmpty(user)) {
			setAttr("msg", "请先登录");
			toLogin();
			return;
		}
		
		this.setAttr("user", user);
		render("/eova/user/userInfo.html");
	}
	
	/**
	 * 获取菜单JSON
	 */
	public void showTree() {
		// 获取父节点
//		Integer rootId = getParaToInt(0);
		Integer pId = this.getParaToInt("pId");
		if (pId == null) {
			renderJson("系统异常");
			return;
		}
		// 获取登录用户的角色
		User user = getSessionAttr(EovaConst.USER);
		//int rid = user.getRid();

		// 获取所有菜单信息
		LinkedHashMap<Integer, Menu> allMenu = (LinkedHashMap<Integer, Menu>) sm.auth.getByParentId(pId);
		// 格式化EasyUI Tree Data
		WidgetUtil.formatEasyTree(allMenu);
		// 根据角色获取已授权菜单Code
		List<String> authMenuCodeList = sm.auth.queryMenuCodeByRid(user.getRids());

		// 获取已授权菜单
		LinkedHashMap<Integer, Menu> authMenu = new LinkedHashMap<Integer, Menu>();
		for (Map.Entry<Integer, Menu> map : allMenu.entrySet()) {
			Menu menu = map.getValue();
			// TODO 仅可查看已授权部分
			if (authMenuCodeList.contains(menu.getStr("code"))) {
				authMenu.put(map.getKey(), menu);
			}
		}

		// 获取已授权子菜单的所有上级节点(若功能有授权，需要找到上级才能)
		LinkedHashMap<Integer, Menu> authParent = new LinkedHashMap<Integer, Menu>();
		for (Map.Entry<Integer, Menu> map : authMenu.entrySet()) {
			WidgetUtil.getParent(allMenu, authParent, map.getValue());
		}

		// 根节点不显示排除
		authParent.remove(pId);

		// 将已授权的子菜单 放入 已授权 父菜单 Map
		// 顺序说明：父在前，子在后,子默认又是有序的
		authParent.putAll(authMenu);

		// Map 转 Tree Json
		String json = WidgetUtil.menu2TreeJson(authParent, pId);

		renderJson(json);
	}
	
	private List<Menu> getTopMenu(){
		User user = getSessionAttr(EovaConst.USER);

		// 获取所有菜单
		List<Menu> menus = Menu.dao.queryMenu();
		// 获取已授权菜单ID
		List<Integer> ids = Button.dao.queryMenuIdByRid(user.getRids());

		// 递归查找已授权功能的上级节点
		HashSet<Integer> authPid = new HashSet<Integer>();
		for (Integer id : ids) {
			Menu m = getParent(menus, id);
			

			
			findParent(authPid, menus, m);	
		}
		
		Iterator<Menu> it = menus.iterator();
		while (it.hasNext()) {
			Menu m = it.next();
			// 构建ztree link属性
			m.put("link", m.getUrl());
			m.remove("url");
			
			Integer mid = m.getInt("id");

			// 已授权目录
			if (authPid.contains(mid) && "0".equals(m.get("parent_id").toString()))
				continue;
			else
				it.remove();
		}
		return menus;
	}
	
	/**
	 * 已授权菜单
	 */
	public void menu() {
		String parentId = getPara("parentId");
		
		
		User user = getSessionAttr(EovaConst.USER);

		// 获取所有菜单
		List<Menu> menus = Menu.dao.queryMenu();
		// 获取已授权菜单ID
		List<Integer> ids = Button.dao.queryMenuIdByRid(user.getRids());

		// 递归查找已授权功能的上级节点
		HashSet<Integer> authPid = new HashSet<Integer>();
		for (Integer id : ids) {
			Menu m = getParent(menus, id);
			

			
			findParent(authPid, menus, m);	
		}

		Iterator<Menu> it = menus.iterator();
		while (it.hasNext()) {
			Menu m = it.next();
			// 构建ztree link属性
			m.put("link", m.getUrl());
			m.remove("url");
			
			if(!xx.isEmpty(parentId) && !parentId.equals(m.get("parent_id").toString()))
				it.remove();
			
			Integer mid = m.getInt("id");

			// 已授权目录
			if (authPid.contains(mid))
				continue;

			// 移除未授权菜单
			if (!ids.contains(mid))
				it.remove();
			
			
		}

		renderJson(JsonKit.toJson(menus));
	}

	/**
	 * 初始化操作
	 */
//	public void init() {
//		render("/eova/init.html");
//	}

	/**
	 * 升级操作
	 */
	public void upgrade() {
		String isUpgrade = EovaConfig.getProps().get("isUpgrade");
		if (xx.isEmpty(isUpgrade) || !isUpgrade.equals("true")) {
			renderText("未开启升级模式，请启动配置 isUpgrade = true, 用完之后立马关掉,后果自负!");
			return;
		}

		render("/eova/help/upgrade.html");
	}

	/**
	 * 初始化操作
	 */
//	public void doInit() {
//		String ip = getPara("ip");
//		String port = getPara("port");
//		String userName = getPara("userName");
//		String password = getPara("password");
//
//		keepPara(ip);
//		keepPara(port);
//		keepPara(userName);
//		keepPara(password);
//
//		String local_url = MessageFormat.format("jdbc:mysql://{0}:{1}/web?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull", ip, port);
//		// String local_user = userName;
//		// String local_pwd = password;
//
//		// String local_eova_url = MessageFormat.format("jdbc:mysql://{0}:{1}/eova?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull", ip, port);
//		// String local_eova_user = userName;
//		// String local_eova_pwd = password;
//
//		// 自动生成JDBC配置 to eova.config
//		String msg = JdbcUtil.initConnection(local_url, userName, password);
//		if (msg != null) {
//
//			if (msg.startsWith("Communications link failure")) {
//				msg = "无法连接数据库，请检查IP:Port";
//			} else if (msg.startsWith("Access denied for user")) {
//				msg = "用户无权限访问，请检查用户名和密码";
//			}
//
//			setAttr("msg", msg);
//			init();
//			return;
//		}
//
//	}
	
	/**
	 * 恢复系统初始配置库
	 */
//	public void doInitSys() {
//
//
//		String password = getPara("password");
//		String init_password = xx.getConfig("init_password");
//		if(StringUtils.isEmpty(init_password) ){
//			renderText( "禁止恢复。");
//			return;
//		}else if(!init_password.equals(password)){
//			renderText("恢复密码错误。");
//			return;
//		}
//
//		String init_file = xx.getConfig("init_file");
//		if(StringUtils.isEmpty(init_file)){
//			renderText("恢复文件未配置。");
//			return;
//		}
//
//		Db.use(xx.DS_EOVA).initSqlFile(init_file);
//
//		renderText("执行恢复sql执行完成。");
//	}

	
	/**
	 * 获取父菜单
	 *
	 * @param menus
	 * @param id
	 * @return
	 */
	private static Menu getParent(List<Menu> menus, int id) {
		for (Menu m : menus) {
			if (m.getInt("id") == id) {
				return m;
			}
		}
		return null;
	}

	/**
	 *
	 * 递归向上查找父节点
	 *
	 * @param authPid 找到的父节点
	 * @param menus 所有菜单
	 */
	private void findParent(HashSet<Integer> authPid, List<Menu> menus, Menu m) {
		if (m == null) {
			return;
		}
		Integer pid = m.getInt("parent_id");
		if (pid == 0) {
			return;
		}
		authPid.add(pid);

		Menu p = getParent(menus, pid);
		findParent(authPid, menus, p);
	}
}