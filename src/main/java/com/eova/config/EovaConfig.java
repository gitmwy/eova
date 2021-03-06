/**
 * Copyright (c) 2013-2017, Jieven. All rights reserved.
 * <p/>
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.druid.wall.WallFilter;
import com.eova.cloud.AuthCloud;
import com.eova.common.plugin.quartz.QuartzPlugin;
import com.eova.common.utils.io.FileUtil;
import com.eova.common.utils.xx;
import com.eova.core.IndexController;
import com.eova.core.auth.AuthController;
import com.eova.core.button.ButtonController;
import com.eova.core.menu.MenuController;
import com.eova.core.meta.MetaController;
import com.eova.core.object.ObjectController;
import com.eova.core.task.TaskController;
import com.eova.handler.UrlBanHandler;
import com.eova.interceptor.AuthInterceptor;
import com.eova.interceptor.LoginInterceptor;
import com.eova.model.Button;
import com.eova.model.EovaLog;
import com.eova.model.Menu;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.model.Role;
import com.eova.model.RoleBtn;
import com.eova.model.RoleObjField;
import com.eova.model.Task;
import com.eova.model.User;
import com.eova.model.Widget;
import com.eova.service.ServiceManager;
import com.eova.template.common.config.TemplateConfig;
import com.eova.template.masterslave.MasterSlaveController;
import com.eova.template.single.SingleController;
import com.eova.template.singlechart.SingleChartController;
import com.eova.template.singleimg.SingleImgController;
import com.eova.template.singletree.SingleTreeController;
import com.eova.template.treetogrid.TreeToGridController;
import com.eova.user.UserController;
import com.eova.widget.WidgetController;
import com.eova.widget.form.FormController;
import com.eova.widget.grid.GridController;
import com.eova.widget.tree.TreeController;
import com.eova.widget.treegrid.TreeGridController;
import com.eova.widget.upload.UploadController;
import com.eova.widget.upload.UploadQiniuController;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.IDataSourceProvider;
import com.jfinal.plugin.activerecord.dialect.AnsiSqlDialect;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.OracleDialect;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;
import com.jfinal.plugin.activerecord.dialect.SqlServerDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.druid.DruidStatViewHandler;
import com.jfinal.plugin.druid.IDruidStatViewAuth;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import org.beetl.core.GroupTemplate;
import org.beetl.ext.jfinal.BeetlRenderFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class EovaConfig extends JFinalConfig {

	/** EOVA所在数据库的类型 **/
	public static String EOVA_DBTYPE = "mysql";
	
	/**
	 * 主业务库
	 */
	public static String MAIN_DBTYPE = "mysql";
	
	/** 数据库命名规则-是否自动小写 **/
	public static boolean isLowerCase = true;

	/** Eova配置属性 **/
	protected static Map<String, String> props = new HashMap<>();
	/** 数据源列表 **/
	protected static Set<String> dataSources = new HashSet<>();
	/** Eova表达式集合 **/
	protected static Map<String, String> exps = new HashMap<>();
	/** URI授权集合<角色ID,URI> **/
	protected static Map<Integer, Set<String>> auths = new HashMap<>();

	private long startTime = 0;

	public static Map<String, String> getProps() {
		return props;
	}

	public static Set<String> getDataSources() {
		return dataSources;
	}

	public static Map<String, String> getExps() {
		return exps;
	}

	public static Map<Integer, Set<String>> getAuths() {
		return auths;
	}

	/**
	 * 系统启动之后
	 */
	@Override
	public void afterJFinalStart() {
		System.err.println("JFinal Started\n");
		xx.costTime(startTime);

		{
			boolean isInit = xx.getConfigBool("initPlugins", true);
			if (isInit) {
				EovaInit.initPlugins();
			}
		}
		{
			boolean isInit = xx.getConfigBool("initSql", false);
			if (isInit && EOVA_DBTYPE.equals(JdbcUtils.MYSQL)) {
				EovaInit.initCreateSql();
			}
		}
	}

	/**
	 * 系统停止之前
	 */
	@Override
	public void beforeJFinalStop() {
	}

	/**
	 * 配置常量
	 */
	@Override
	public void configConstant(Constants me) {
		startTime = System.currentTimeMillis();

		System.err.println("Config Constants Starting...");
		me.setEncoding("UTF-8");
		// 初始化配置
		EovaInit.initConfig(props);
		
		String env = EovaConfig.getProps().get("env");
		if("DEV".equalsIgnoreCase(env) || "TEST".equalsIgnoreCase(env)  ){
			// 开发模式
			me.setDevMode(true);
			EovaConfig.getProps().put("isCaptcha", "false");
		}else{
			me.setDevMode(false);
			EovaConfig.getProps().put("isCaptcha", "true");
		}

		// 设置主视图为Beetl
		me.setMainRenderFactory(new BeetlRenderFactory());
		// POST内容最大500M(安装包上传)
		me.setMaxPostSize(1024 * 1024 * 500);

		me.setError500View("/eova/500_lay.html");
		me.setError404View("/eova/404_lay.html");

		// 设置静态根目录为上传根目录
		me.setBaseUploadPath(xx.getConfig("static_root"));

		@SuppressWarnings("unused")
		GroupTemplate group = BeetlRenderFactory.groupTemplate;

		// 设置全局变量
		final String STATIC = props.get("domain_static");
		final String CDN = props.get("domain_cdn");
		final String IMG = props.get("domain_img");
		final String FILE = props.get("domain_file");

		Map<String, Object> sharedVars = new HashMap<String, Object>();
		if (!xx.isEmpty(STATIC))
			sharedVars.put("STATIC", STATIC);
		else
			sharedVars.put("STATIC", "");
		if (!xx.isEmpty(CDN))
			sharedVars.put("CDN", CDN);
		if (!xx.isEmpty(IMG))
			sharedVars.put("IMG", IMG);
		if (!xx.isEmpty(FILE))
			sharedVars.put("FILE", FILE);


		sharedVars.put("UPLOAD_IMG_SIZE", EovaConst.UPLOAD_IMG_SIZE);
		sharedVars.put("UPLOAD_FILE_SIZE", EovaConst.UPLOAD_FILE_SIZE);
		
		sharedVars.put("UPLOAD_IMG_TYPE", FileUtil.IMG_TYPE );
		sharedVars.put("UPLOAD_FILE_TYPE", FileUtil.ALL_TYPE );


		sharedVars.put("APP", AuthCloud.getEovaApp());

		// Load Template Const
		PageConst.init(sharedVars);

		BeetlRenderFactory.groupTemplate.setSharedVars(sharedVars);

		// 初始化配置
		exp();// Eova表达式
		auth();// URI授权
	}

	/**
	 * 配置路由
	 */
	@Override
	public void configRoute(Routes me) {
		System.err.println("Config Routes Starting...");

		// 业务模版
		me.add("/" + TemplateConfig.SINGLE_GRID, SingleController.class);
		me.add("/" + TemplateConfig.SINGLE_TREE, SingleTreeController.class);
		me.add("/" + TemplateConfig.SINGLE_CHART, SingleChartController.class);
		me.add("/" + TemplateConfig.MASTER_SLAVE_GRID, MasterSlaveController.class);
		me.add("/" + TemplateConfig.TREE_GRID, TreeToGridController.class);
		
		me.add("/" + TemplateConfig.IMG_GRID, SingleImgController.class);
		me.add("/"+TemplateConfig.ORG_GRID, com.eova.template.org.OrgController.class);
		
		// 组件
		me.add("/widget", WidgetController.class);
		//上传组件(有多种，实现了7牛)
		if(xx.getConfig("oss_static","0").equals("0"))
			me.add("/upload", UploadController.class);
		else
			me.add("/upload", UploadQiniuController.class);
		
		
		me.add("/form", FormController.class);
		me.add("/grid", GridController.class);
		me.add("/tree", TreeController.class);
		me.add("/treegrid", TreeGridController.class);
		
		
		// Eova业务
		me.add("/meta", MetaController.class);
		me.add("/menu", MenuController.class);
		me.add("/button", ButtonController.class);
		me.add("/auth", AuthController.class);
		me.add("/task", TaskController.class);
		me.add("/obj", ObjectController.class);
		
		// me.add("/cloud", CloudController.class);
		
		me.add("/user", UserController.class);
		
		LoginInterceptor.excludes.add("/cloud");

		// 自定义路由
		route(me);

		// 如果有自定义，将不再注册系统默认实现
		if (me.getViewPath("/") == null) {
			me.add("/", IndexController.class);
		}

		// 初始化Eova表达式配置
		auth();
	}
	
	/**
	 * 强制覆盖路由（jfinal不提供覆盖）
	 * @param me
	 * @param controllerKey
	 * @param controllerClass
	 */
	public void forceAddRoute(Routes me,String controllerKey, Class<? extends Controller> controllerClass ){
		Set<Entry<String, Class<? extends Controller>>> all = me.getEntrySet(); 
		Entry<String, Class<? extends Controller>> taget = null; 
		for (Entry<String, Class<? extends Controller>> one: all) {  
			String key =  one.getKey();
		      if(controllerKey.equalsIgnoreCase(key)){
		    	  taget = one;
		    		 break;
		      }         
		}  
		
		if(taget != null){
			System.out.println("Route "+controllerKey +" repaced~~");
			all.remove(taget);
			me.add(controllerKey, controllerClass);
		}
	}

	/**
	 * 配置插件
	 */
	@Override
	public void configPlugin(Plugins plugins) {
		System.err.println("Config Plugins Starting...");

		String eovaUrl, eovaUser, eovaPwd;
		String mainUrl, mainUser, mainPwd;

		eovaUrl = xx.getConfig("eova_url");
		eovaUser = xx.getConfig("eova_user");
		eovaPwd = xx.getConfig("eova_pwd");

		mainUrl = xx.getConfig("main_url");
		mainUser = xx.getConfig("main_user");
		mainPwd = xx.getConfig("main_pwd");

		// 默认数据源
		{
			DruidPlugin dp = initDruidPlugin(mainUrl, mainUser, mainPwd);
			ActiveRecordPlugin arp = initActiveRecordPlugin(mainUrl, xx.DS_MAIN, dp);
			System.out.println("load data source:" + mainUrl + "/" + mainUser);

			mapping(arp);

			plugins.add(dp).add(arp);

			// 设置数据库方言，默认为 MysqlDialect
			//
			
			// 注册数据源
			dataSources.add(xx.DS_MAIN);
			
			try {
				//Eova的数据库类型
				MAIN_DBTYPE = JdbcUtils.getDbType(mainUrl, JdbcUtils.getDriverClassName(mainUrl));
				
				if(MAIN_DBTYPE.equals(JdbcUtils.SQL_SERVER))
					arp.setDialect(new SqlServerDialect());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// Eova数据源
		{
			DruidPlugin dp = initDruidPlugin(eovaUrl, eovaUser, eovaPwd);
			ActiveRecordPlugin arp = initActiveRecordPlugin(eovaUrl, xx.DS_EOVA, dp);
			System.out.println("load eova datasource:" + eovaUrl + "/" + eovaUser);

			mappingEova(arp);

			plugins.add(dp).add(arp);

			// 注册数据源
			dataSources.add(xx.DS_EOVA);

			try {
				// Eova的数据库类型
				EOVA_DBTYPE = JdbcUtils.getDbType(eovaUrl, JdbcUtils.getDriverClassName(eovaUrl));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// 自定义插件
		plugin(plugins);

		// 初始化ServiceManager
		ServiceManager.init();

		// 配置EhCachePlugin插件
		plugins.add(new EhCachePlugin());

		// 配置Quartz
		boolean isQuartz = xx.getConfigBool("isQuartz", true);
		if (isQuartz) {
			QuartzPlugin quartz = new QuartzPlugin();
			plugins.add(quartz);
		}
	}

	/**
	 * 配置全局拦截器
	 */
	@Override
	public void configInterceptor(Interceptors me) {
		System.err.println("Config Interceptors Starting...");
		// JFinal.me().getServletContext().setAttribute("EOVA", "简单才是高科技");
		
		// 登录验证
		me.add(new LoginInterceptor());
		// 权限验证拦截
		me.add(new AuthInterceptor());
		
//		//mssql专用拦截（mssql特殊属性）
//		me.add(new MsqlInterceptor());
	}

	/**
	 * 配置处理器
	 */
	@Override
	public void configHandler(Handlers me) {
		System.err.println("Config Handlers Starting...");
		// 添加DruidHandler
		//DruidStatViewHandler dvh = new DruidStatViewHandler("/druid");
		 DruidStatViewHandler dvh = new
				  DruidStatViewHandler("/druid", new IDruidStatViewAuth() {
				    public boolean isPermitted(HttpServletRequest request) {
				      HttpSession hs = request.getSession(false);
				    
				  	User user = (User)hs.getAttribute(EovaConst.USER) ;
				  	if(user != null && user.isAdmin()){
				  		
				      return true;
				    }else{
				    	return false;
				    }
				    }
				  });
		 
		me.add(dvh);
		// 过滤禁止访问资源
		me.add(new UrlBanHandler(".*\\.(html|tag)", false));
	}

	/**
	 * Eova Data Source Model Mapping
	 *
	 * @param arp
	 */
	protected void mappingEova(ActiveRecordPlugin arp) {
		arp.addMapping("eova_object", MetaObject.class);
		arp.addMapping("eova_field", MetaField.class);
		arp.addMapping("eova_button", Button.class);
		arp.addMapping("eova_menu", Menu.class);
		arp.addMapping("eova_user", User.class);
		
		arp.addMapping("eova_role", Role.class);
		arp.addMapping("eova_role_btn", RoleBtn.class);
		arp.addMapping("eova_log", EovaLog.class);
		arp.addMapping("eova_task", Task.class);
		arp.addMapping("eova_widget", Widget.class);
		
		arp.addMapping("bb_role_obj_field", RoleObjField.class);
	}

	/**
	 * Main Data Source Model Mapping
	 *
	 * @param arp
	 */
	protected void mapping(ActiveRecordPlugin arp) {
	}

	/**
	 * Custom Route
	 *
	 * @param me
	 */
	protected void route(Routes me) {
	}

	/**
	 * Custom Plugin
	 *
	 * @param plugins
	 * @return
	 */
	protected void plugin(Plugins plugins) {
	}

	/**
	 * init Druid
	 *
	 * @param url JDBC
	 * @param username 数据库用户
	 * @param password 数据库密码
	 * @return
	 */
	protected DruidPlugin initDruidPlugin(String url, String username, String password) {
		// 设置方言
		WallFilter wall = new WallFilter();
		String dbType;
		try {
			dbType = JdbcUtils.getDbType(url, JdbcUtils.getDriverClassName(url));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		wall.setDbType(dbType);

		DruidPlugin dp = new DruidPlugin(url, username, password);
		dp.addFilter(new StatFilter());
		dp.addFilter(wall);
		return dp;

	}

	/**
	 * init ActiveRecord
	 *
	 * @param url JDBC
	 * @param ds DataSource
	 * @param dp Druid
	 * @return
	 */
	protected ActiveRecordPlugin initActiveRecordPlugin(String url, String ds, IDataSourceProvider dp) {
		ActiveRecordPlugin arp = new ActiveRecordPlugin(ds, dp);
		// 提升事务级别保证事务回滚
		int lv = xx.toInt(xx.getConfig("transaction_level"), Connection.TRANSACTION_REPEATABLE_READ);
		arp.setTransactionLevel(lv);

		String dbType;
		try {
			dbType = JdbcUtils.getDbType(url, JdbcUtils.getDriverClassName(url));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		// 统一全部默认小写
		arp.setContainerFactory(new CaseInsensitiveContainerFactory(isLowerCase));

		Dialect dialect;
		if (JdbcUtils.MYSQL.equalsIgnoreCase(dbType) || JdbcUtils.H2.equalsIgnoreCase(dbType)) {
			dialect = new MysqlDialect();
		} else if (JdbcUtils.ORACLE.equalsIgnoreCase(dbType)) {
			dialect = new OracleDialect();
			((DruidPlugin) dp).setValidationQuery("select 1 FROM DUAL");
		} else if (JdbcUtils.POSTGRESQL.equalsIgnoreCase(dbType)) {
			dialect = new PostgreSqlDialect();
		}else if (JdbcUtils.SQL_SERVER.equalsIgnoreCase(dbType)) {
			dialect = new SqlServerDialect();
		}  else {
			// 默认使用标准SQL方言
			dialect = new AnsiSqlDialect();
		}
		arp.setDialect(dialect);

		// 是否显示SQL
		Boolean isShow = xx.toBoolean(xx.getConfig("showSql"), false);
		if (isShow != null) {
			arp.setShowSql(isShow);
		}

		return arp;
	}

	/**
	 * Eova Expression Mapping
	 *
	 */
	protected void exp() {
		// Eova 系统功能需要的Exp
		exps.put("selectEovaFieldByObjectCode", "select en Field,cn Name from eova_field where object_code = ?;ds=eova");
		
		//系统库 省市区 级联，如果表不满足需求可以覆盖
		exps.put("selectAreaByLv2AndPid", "select areaId ID,areaName CN from area where level = 2  and parentId = ?;ds=eova");
        exps.put("selectAreaByLv3AndPid", "select areaId ID,areaName CN from area where level = 3  and parentId = ?;ds=eova");
	
	}

	/**
	 * URI Auth
	 */
	protected void auth() {

		// 公有授权白名单
		HashSet<String> uris = new HashSet<String>();
		uris.add("/widget/**");
		uris.add("/upload/**");
		uris.add("/widget/**");
		uris.add("/meta/object/**");
		uris.add("/meta/fields/**");
		
		//uris.add("/druid/**");
		
		
		auths.put(0, uris);

	}

}