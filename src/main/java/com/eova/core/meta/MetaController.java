/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.core.meta;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.cloud.EovaCloud;
import com.eova.common.Easy;
import com.eova.common.utils.xx;
import com.eova.common.utils.db.DbUtil;
import com.eova.common.utils.db.DsUtil;
import com.eova.config.EovaConfig;
import com.eova.config.EovaConst;
import com.eova.engine.EovaExp;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.model.RoleObjField;
import com.eova.model.User;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.activerecord.tx.TxConfig;

/**
 * 元数据相关获取
 *
 * @author Jieven
 *
 */
public class MetaController extends Controller {

	// 获取元对象
	public void object() {
		String code = getPara(0);
		MetaObject mo = MetaObject.dao.getByCode(code);
		// 安全保护，去掉界面不需要的字段
		mo.remove("filter");
		mo.remove("table_name");
		mo.remove("view_name");
		mo.remove("view_sql");
		renderJson(JsonKit.toJson(mo));
	}

	// 获取元字段集(未登录全部，登录后根据角色返回字段)
	public void fields() {
		String code = getPara(0);
		User user = (User)this.getSession().getAttribute(EovaConst.USER);
		List<MetaField> fields = MetaField.dao.queryFields(code);
		
		List<Integer> roleFields = null;
		if(user != null){
			roleFields =	RoleObjField.dao.queryIdByRids(user.getRids(),code);
		}
		
		// 安全保护，去掉界面不需要的字段
		for (MetaField f : fields) {
//			f.remove("exp"); //保留
			f.remove("table_name");
			f.remove("data_type");
			f.remove("data_type_name");
			f.remove("data_size");
			f.remove("date_decimal");
			
			//f.getEn()
			//特殊字段"[]"包裹，需去掉
//			String en =(String) f.get("en");
//			if(en!= null && en.startsWith("[") && en.endsWith("]"))
//				f.put("en", en.substring(1, en.length()-1));
				
			//f.get(attr)
			if(roleFields != null && roleFields.size() != 0){
				if(!roleFields.contains(f.get("id"))){
					f.put("is_show", false);
				}
			}
		}
		
		renderJson(JsonKit.toJson(fields));
	}

	// 编辑元数据
	public void edit() {
		String objectCode = getPara(0);
		setAttr("objectCode", objectCode);
		render("/eova/meta/edit.html");
	}

	// 导入页面
	public void imports() {
		setAttr("dataSources", EovaConfig.getDataSources());
		render("/eova/meta/import.html");
	}

	// 查找表结构表头
	public void find() {

		String ds = getPara(0);
		String type = getPara(1);
		// 根据表达式手工构建Eova_Object
		MetaObject eo = MetaObject.dao.getTemplate();
		eo.put("data_source", ds);
		// 第1列名
		eo.put("pk_name", "table_name");
		// 第2列名
		eo.put("cn", "table_name");

		// 根据表达式手工构建Eova_Item
		List<MetaField> eis = new ArrayList<MetaField>();
		eis.add(EovaExp.buildItem(1, "table_name", "编码", false));
		eis.add(EovaExp.buildItem(2, "table_name", "表名", true));

		setAttr("objectJson", JsonKit.toJson(eo));
		setAttr("fieldsJson", JsonKit.toJson(eis));
		setAttr("itemList", eis);
		setAttr("pk", "pk_name");

		setAttr("action", "/meta/findJson/" + ds + '-' + type);
		setAttr("isPaging", false);

		render("/eova/widget/find/find.html");
	}

	// 查找表结构数据
	public void findJson() {

		// 获取数据库
		String ds = getPara(0);
		String type = getPara(1);

		// 用户过滤
		String schemaPattern = null;
		// Oracle需要根据用户名过滤表
		if (xx.isOracle()) {
			schemaPattern = DsUtil.getUserNameByConfigName(ds);
		}

		// 表名过滤
		String tableNamePattern = getPara("query_table_name");
		if (!xx.isEmpty(tableNamePattern)) {
			tableNamePattern = "%" + tableNamePattern + "%";
		}

		List<String> tables = DsUtil.getTableNamesByConfigName(ds, type, schemaPattern, tableNamePattern);
		JSONArray tableArray = new JSONArray();
		for (String tableName : tables) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("table_name", tableName);
			tableArray.add(jsonObject);
		}
		// 将分页数据转换成JSON
		String json = JsonKit.toJson(tableArray);
		json = "{\"total\":" + tableArray.size() + ",\"rows\":" + json + "}";
		renderJson(json);
	}

	// 一键导入
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void importAll() {

		System.out.println("MetaController.importAll begins...");
		
		if (!getPara(0, "").equals("eova")) {
			renderJson(new Easy("请输入校验码，防止误操作！！！！！"));
			return;
		}

		boolean isUpgrade = xx.getConfigBool("isUpgrade", false);
		if (!isUpgrade) {
			renderText("未开启升级模式，请启动配置 isUpgrade = true");
			return;
		}

		String ds = xx.DS_MAIN;
		String type = DsUtil.TABLE;

		// DB名
		String db = DsUtil.getDbNameByConfigName(ds);

		// 获取所有表名
		List<String> tables = DsUtil.getTableNamesByConfigName(ds, type, null, null);

		for (String table : tables) {

			if (table.startsWith("eova_") || table.equals("dicts") || table.equals("user_info")) {
				continue;
			}
			// if (!table.equals("dicts")) {
			// continue;
			// }
			System.out.println(table);

			String name = table;
			String code = db + "_" + table;

			if (xx.isMysql()) {
				String s = Db.queryStr("select TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?", db, table);
				if (!xx.isEmpty(s)) {
					name = s;
				}
			}

			// 导入元数据
			importMeta(ds, type, table, name, code, "id");
		}

		renderJson(new Easy());
	}

	// 导入元数据
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void doImports() {

		System.out.println("MetaController.doImports begins...");
		
		String ds = getPara("ds");
		String type = getPara("type");
		String table = getPara("table");
		String name = getPara("name");
		String code = getPara("code");
		String pk = getPara("pk");

		if (xx.isOneEmpty(ds, type, table, name, code, pk)) {
			renderJson(new Easy("参数都必须填写！"));
			return;
		}

		MetaObject o = MetaObject.dao.getByCode(code);
		if (o != null) {
			renderJson(new Easy(String.format("对象编码[%s]已经被其它对象使用了，请修改对象编码！", code)));
			return;
		}

		// 导入元数据
		importMeta(ds, type, table, name, code, pk);

		renderJson(new Easy());
	}

	// 导出选中元数据
	public void doExport() {
		String ids = getPara(0);

		StringBuilder sb = new StringBuilder();

		String sql1 = "select * from eova_object where id in (" + ids + ")";
		List<Record> objects = Db.use(xx.DS_EOVA).find(sql1);
		DbUtil.generateSql(objects, "eova_object", "id", sb);

		sb.append("\n\n");

		String sql2 = "select * from eova_field where object_code in (select code from eova_object where id in (" + ids + "))";
		List<Record> fields = Db.use(xx.DS_EOVA).find(sql2);
		DbUtil.generateSql(fields, "eova_field", "id", sb);

		renderText(sb.toString());
	}

	// 覆盖同步元字段(主键需要更新元对象)
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void override() {
		String str = getPara(0);
		String[] ids = str.split(",");
		for (String id : ids) {
			MetaObject o = MetaObject.dao.findById(id);
			// 删除元数据
			MetaObject.dao.deleteById(id);
			MetaField.dao.deleteByObjectCode(o.getCode());
			// 导入元数据
			importMetaObject(o.getDs(), o.getType(), o.getView(), o.getName(), o.getCode(), o.getPk());
			importMetaField(o.getDs(), o.getView(), o.getCode());
		}

		renderJson(new Easy());
	}

	// 增量同步元字段
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void syncnew() {
		String str = getPara(0);
		String[] ids = str.split(",");
		for (String id : ids) {
			MetaObject mo = MetaObject.dao.findById(id);
			List<MetaField> fields = MetaField.dao.queryByObjectCode(mo.getCode());

			String ds = mo.getDs();
			String table = mo.getView();

			JSONArray list = DsUtil.getColumnInfoByConfigName(ds, table);

			// 如果当前元字段中不存在就新增
			for (int i = 0; i < list.size(); i++) {
				JSONObject o = list.getJSONObject(i);
				String name = o.getString("COLUMN_NAME");

				// 是否新字段
				boolean isNew = true;
				for (MetaField field : fields) {
					if (name.equalsIgnoreCase(field.getEn())) {
						isNew = false;
					}
				}

				// 新字段进行导入
				if (isNew) {
					ColumnMeta col = new ColumnMeta(ds, table, o);
					MetaField mi = new MetaField(mo.getCode(), col);
					mi.save();
				}
			}
		}

		renderJson(new Easy());
	}

	// 复制元数据
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void copy() {
		String id = getPara(0);
		String code = getPara(1);

		MetaObject object = MetaObject.dao.findById(id);
		List<MetaField> fields = MetaField.dao.queryByObjectCode(object.getCode());

		// 使用新的对象编码,生成一份元数据
		for (MetaField f : fields) {
			f.remove("id");
			f.set("object_code", code);
			f.save();
		}
		object.remove("id");
		object.set("code", code);
		object.save();

		renderJson(new Easy());
	}

	/**
	 * 导入元数据
	 *
	 * @param ds 数据源
	 * @param type 表还是视图
	 * @param table 表名
	 * @param name 对象名
	 * @param code 对象编码
	 * @param pk 主键名
	 */
	private void importMeta(String ds, String type, String table, String name, String code, String pk) {
		
		System.out.println("MetaController.importMeta begins...");
		// table自动获取主键
		if (type.equalsIgnoreCase(DsUtil.TABLE)) {
			pk = DsUtil.getPkName(ds, table);
			if (xx.isEmpty(pk)) {
				renderJson(new Easy("主键不能为空，必须设置主键！"));
				return;
			}
		}

		// 导入元字段
		importMetaField(ds, table, code);
		// 导入元对象
		importMetaObject(ds, type, table, name, code, pk);

		// 云端人工智能预处理元数据
		EovaCloud.buildMeta(code);
	}

	/**
	 * 导入元字段
	 *
	 * @param code 对象编码
	 * @param list 字段元数据
	 * @param ds 数据源
	 * @param table 表名
	 */
	private void importMetaField(String ds, String table, String code) {
		
		System.out.println("MetaController.importMeta begins...");
		
		JSONArray list = DsUtil.getColumnInfoByConfigName(ds, table);

		for (int i = 0; i < list.size(); i++) {
			JSONObject o = list.getJSONObject(i);

			ColumnMeta col = new ColumnMeta(ds, table, o);
			MetaField mi = new MetaField(code, col);
			mi.save();

			// 自动根据字典 将对应的字段 设置成 下拉框 并生成表达式
			String remarks = o.getString("REMARKS");
			if (xx.isEmpty(remarks)) {
				continue;
			}
			//eg. 状态:/：1=/-上架,/，2=/-售罄,3=下架 ,4=过期  ，，分割符号：=|-|——
			if ((remarks.contains(":") || remarks.contains("：")) 
					&& (remarks.contains("=") || remarks.contains("-") || remarks.contains("——"))) {
				String dictTable = EovaConfig.getProps().get("main_dict_table");
				String exp = String.format("select value ID,name CN from %s where object = '%s' and field = '%s'", dictTable, table, mi.getEn());
				String sql = "update eova_field set type = '下拉框', exp = ? where object_code = ? and en = ?";
				Db.use(xx.DS_EOVA).update(sql, exp, code, mi.getEn());
				System.out.println("自动绑定字典成功");
			}
		}
	}

	/**
	 * 导入元对象
	 *
	 * @param ds
	 * @param type
	 * @param table
	 * @param name
	 * @param code
	 * @param pkName
	 */
	private void importMetaObject(String ds, String type, String table, String name, String code, String pkName) {
		
		System.out.println("MetaController.importMeta begins...");
		
		MetaObject mo = new MetaObject();
		// 编码
		mo.set("code", code);
		// 名称
		mo.set("name", name);
		// 主键
		mo.set("pk_name", pkName.toLowerCase());
		// 数据源
		mo.set("data_source", ds);
		// 表或视图
		if (type.equalsIgnoreCase(DsUtil.TABLE)) {
			mo.set("table_name", table.toLowerCase());
		} else {
			mo.set("view_name", table.toLowerCase());
			if (xx.isMysql()) {
				// TODO 暂停自动配置视图，手工在拦截器里实现 新增，修改，删除
				// 获取视图的sql语句 PS:不自动获取手写也行，但是必须是标准带别名sql
				// String db = DsUtil.getDbNameByConfigName(ds);
				// Record recrod = Db.use(xx.DS_EOVA).findFirst("select VIEW_DEFINITION from information_schema.VIEWS v where v.TABLE_SCHEMA = ? and v.TABLE_NAME = ?", db, table);
				// String sql = recrod.getStr("VIEW_DEFINITION");
				// mo.set("view_sql", sql);

				// 解析View Sql
				// ViewFactory vf = new ViewFactory(mo.getStr("view_sql"));
				// 自动生成元数据配置
				// MetaObjectConfig config = new MetaObjectConfig();
				// config.setView(vf.parse());
				// mo.setConfig(config);
				// // 自动生成元字段关联表名
				// Collection<Column> columns = vf.getColumns();
				// for (Column c : columns) {
				// String en = c.getName();
				// String tableName = DbUtil.getEndName(c.getTable());
				// MetaField.dao.updateTableNameByCode(code, en, tableName);
				// }
			}
		}
		//添加默认拦截器
		mo.set("biz_intercept", "com.eova.aop.MetaObjectIntercept");
		mo.save();
	}

	// public static void main(String[] args) {
	// String s = new MetaController().buildRemarks("状态:1=上架，2=售罄,3=下架 ,4=过期", "eova_log", "type");
	// }

	// 刷新元字段数据类型
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void syncField() {

		List<MetaObject> os = MetaObject.dao.find("select * from eova_object");
		for (MetaObject o : os) {
			List<MetaField> fs = MetaField.dao.queryByObjectCode(o.getCode());

			String ds = o.getDs();
			String table = o.getView();

			JSONArray list = DsUtil.getColumnInfoByConfigName(ds, table);
			for (int i = 0; i < list.size(); i++) {
				JSONObject json = list.getJSONObject(i);
				String name = json.getString("COLUMN_NAME");
				for (MetaField f : fs) {
					// 更新数据类型
					if (f.getEn().equalsIgnoreCase(name)) {
						ColumnMeta col = new ColumnMeta(ds, table, json);
						f.set("data_type", col.dataType);
						f.set("data_type_name", col.dataTypeName);
						f.set("data_size", col.dataSize);
						f.set("data_decimal", col.dataDecimal);
						// f.set("defaulter", col.defaultValue);
						f.set("is_auto", col.isAuto);
						// 自动纠正易时间配置
						if (col.dataTypeName.contains("TIMESTAMP") && col.dataTypeName.contains("DATETIME")) {
							f.set("type", "时间框");
						} else if (col.dataTypeName.contains("DATE")) {
							f.set("type", "日期框");
						}
						f.update();
					}
				}
			}
			LogKit.info("元数据刷新成功：" + o.getCode());
		}

		renderJson(new Easy());
	}

	// 交换序号
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void swap() {
		int sid = getParaToInt(0);
		int snum = getParaToInt(1);
		int tid = getParaToInt(2);
		int tnum = getParaToInt(3);

		MetaField.dao.swapOrderNum(sid, tid, snum, tnum);

		renderJson(new Easy());
	}
}