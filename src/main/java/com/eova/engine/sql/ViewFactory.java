/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.engine.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.stat.TableStat.Column;
import com.alibaba.druid.stat.TableStat.Name;
import com.alibaba.druid.stat.TableStat.Relationship;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.eova.common.utils.xx;
import com.eova.common.utils.db.DbUtil;
import com.eova.common.utils.db.DsUtil;
import com.eova.core.object.config.TableConfig;
import com.eova.engine.SqlParse;

/**
 * 
 * SQL解析工厂
 * 
 * @author Jieven
 * 
 */
public class ViewFactory {
	private final String ds;
	private final String sql;
	private final String dbType;
	private MySqlSchemaStatVisitor vt;

	public ViewFactory(String sql) {
		this(xx.DS_MAIN, sql);
	}

	public ViewFactory(String ds, String sql) {
		this(ds, sql, JdbcConstants.MYSQL);
	}

	public ViewFactory(String ds, String sql, String dbType) {
		this.ds = ds;
		this.sql = sql;
		this.dbType = dbType;

		init();
	}

	private void init() {
		System.out.println(sql);

		List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
		if (stmtList.size() != 1) {
			throw new RuntimeException("View Sql 解析异常");
		}

		SQLStatement stmt = stmtList.get(0);
		vt = new MySqlSchemaStatVisitor();
		stmt.accept(vt);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LinkedHashMap<String, TableConfig> parse() {
		// 1.获取所有表名，自动构建 表的数目
		Map<Name, TableStat> tables = vt.getTables();
		Set<Name> tableNames = tables.keySet();
		Map<String, String> aliasMap = vt.getAliasMap();
		System.out.println("Tables : " + tableNames);
		System.out.println("fields : " + vt.getColumns());
		// System.out.println("Alias  : " + aliasMap);
		// System.out.println();

		// 2.解析SQL，获取查询显示列 a.id = b.id and b.id = c.id
		List<String> selectItem = new ArrayList<>();
		SqlParse sp = new SqlParse(dbType, sql);
		for (SQLSelectItem item : sp.getSelectItem()) {
			SQLPropertyExpr pe = (SQLPropertyExpr) item.getExpr();
			String name = pe.getName();
			String ow = pe.getOwner().toString();
			selectItem.add(aliasMap.get(ow) + '.' + name);
		}
		// System.out.println("Show Field：" + selectItem);

		// 3.获取关系
		Set<Relationship> relationships = vt.getRelationships();
		System.out.println("Ref    : " + relationships);

		// 关系数据清洗
		HashMap<String, Set<String>> refs = new HashMap<String, Set<String>>();
		for (Relationship rs : relationships) {
			String left = rs.getLeft().toString();
			String right = rs.getRight().toString();
			{
				Set<String> set = refs.get(left);
				if (set == null) {
					set = new HashSet<>();
				}
				set.add(right);
				refs.put(left, set);
			}
			{
				Set<String> set = refs.get(right);
				if (set == null) {
					set = new HashSet<>();
				}
				set.add(left);
				refs.put(right, set);
			}
			rs.getRight();
		}
		System.out.println("关系:" + refs);

		LinkedHashMap<String, TableConfig> config = new LinkedHashMap<>();
		for (Name n : tableNames) {
			String tableName = n.getName();
			// System.out.println();
			System.out.println("table: " + tableName);

			TableConfig tc = new TableConfig();

			Iterator iter = refs.entrySet().iterator();
			B: while (iter.hasNext()) {
				// 根据连表关系获取条件字段和条件值
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				Set<String> refFields = (Set<String>) entry.getValue();
				if (DbUtil.compareTable(key, tableName)) {
					// System.out.println("whereField: " + DbUtil.getField(key));
					tc.setWhereField(DbUtil.getEndName(key));
					for (String field : refFields) {
						if (selectItem.contains(field)) {
							// System.out.println("left-paramField: " + DbUtil.getField(field));
							tc.setParamField(DbUtil.getEndName(field));
							break B;
						} else {
							String paramField = findRefField(selectItem, refs.get(field));
							if (paramField != null) {
								// System.out.println("right-paramField: " + paramField);
								tc.setParamField(paramField);
								break B;
							}
						}
					}
				}
			}

			String[] ss = tableName.split("\\.");
			if (ss.length == 2) {
				// db.table 直接取第2项
				tableName = ss[1];
			}

			// 如果存在当前表的主键,则优先使用主键作为条件进行持久化
			String pk = null;
			try {
				pk = DsUtil.getPkName(ds, tableName);
				System.out.println("PK:" + pk);
			} catch (Exception e) {
			}
			// main 测试无法获取 JDBC信息，所以手工模拟
			if (pk == null && DbUtil.compareTable(tableName, "orders")) {
				pk = "id";
			}
			// 如果当前表有主键，并且是当前View显示列，就用这个当条件
			if (!xx.isEmpty(pk)) {
				for (String i : selectItem) {
					// 表名.字段 判定是否为当前表的主键
					if (DbUtil.compareField(i, tableName + "." + pk)) {
						tc.setWhereField(pk);
						tc.setParamField(pk);
						break;
					}
				}
			}

			config.put(tableName, tc);

			// System.out.println();
		}

		return config;
	}

	public Collection<Column> getColumns() {
		Collection<Column> columns = vt.getColumns();
		return columns;
	}

	private String findRefField(List<String> selectItem, Set<String> val) {
		for (String field : val) {
			if (selectItem.contains(field)) {
				return DbUtil.getEndName(field);
			}
		}
		return null;
	}

	public static void main(String[] args) {
		// 1V1 users join users_exp
		{
			String sql = "SELECT `u`.`id` AS `id`, `u`.`status` AS `status`, `u`.`login_id` AS `login_id`, `u`.`login_pwd` AS `login_pwd`, `u`.`nickname` AS `nickname`, `u`.`reg_time` AS `reg_time`, `u`.`info` AS `info`, `u`.`tag` AS `tag`, `ue`.`exp` AS `exp`, `ue`.`avg` AS `avg`, `ue`.`qq` AS `qq` FROM ( `users` `u` JOIN `users_exp` `ue` ) WHERE (`u`.`id` = `ue`.`users_id`)";
			ViewFactory vf = new ViewFactory(sql);
			LinkedHashMap<String, TableConfig> parse = vf.parse();
			System.out.println(xx.formatJson(JSONObject.toJSONString(parse, SerializerFeature.SortField)));
		}
		// 1VN hotel left join hotel_bed,hotel_stock
		{
			String sql = "select `h`.`id` AS `id`,`h`.`name` AS `name`,`hb`.`num` AS `num`,`hs`.`category` AS `category` from ((`demo`.`hotel` `h` left join `demo`.`hotel_bed` `hb` on((`hb`.`hotel_id` = `h`.`id`))) left join `demo`.`hotel_stock` `hs` on((`hs`.`hotel_id` = `h`.`id`)))";
			ViewFactory vf = new ViewFactory(sql);
			LinkedHashMap<String, TableConfig> parse = vf.parse();
			System.out.println(xx.formatJson(JSONObject.toJSONString(parse, SerializerFeature.SortField)));
		}
		// 1V1 orders left join user,address
		{
			String sql = "select `o`.`id` AS `id`,`o`.`state` AS `state`,`o`.`money` AS `money`,`o`.`memo` AS `memo`,`o`.`create_user_id` AS `create_user_id`,`o`.`create_time` AS `create_time`,`o`.`address_id` AS `address_id`,`a`.`name` AS `name`,`a`.`full` AS `full`,`a`.`mobilephone` AS `mobilephone`,`u`.`login_id` AS `login_id`,`u`.`nickname` AS `nickname`,`u`.`info` AS `info` from ((`demo`.`orders` `o` left join `demo`.`users` `u` on((`o`.`create_user_id` = `u`.`id`))) left join `demo`.`address` `a` on((`o`.`address_id` = `a`.`id`)))";
			ViewFactory vf = new ViewFactory(sql);
			LinkedHashMap<String, TableConfig> parse = vf.parse();
			System.out.println(xx.formatJson(JSONObject.toJSONString(parse, SerializerFeature.SortField)));
		}
	}
}