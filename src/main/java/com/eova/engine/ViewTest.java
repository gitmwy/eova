package com.eova.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.stat.TableStat.Name;
import com.alibaba.druid.stat.TableStat.Relationship;
import com.alibaba.druid.util.JdbcConstants;
import com.eova.common.utils.db.DbUtil;
import com.eova.config.EovaConfig;

/**
 * SQL 数据权限解析
 * 
 * @author Administrator
 * 
 */
public class ViewTest {
	public static void main(String[] args) {
		{
			String sql = "SELECT `hs`.`hotel_id`, `h`.`name` AS `name`, `hb`.`num` AS `num`, `hs`.`category` AS `category` FROM (( `hotel` `h` LEFT JOIN `hotel_bed` `hb` ON ((`hb`.`hotel_id` = `h`.`id`))) LEFT JOIN `hotel_stock` `hs` ON ((`hs`.`hotel_id` = `h`.`id`)))";
			parse(sql);
		}
		System.out.println();
		{
			String sql = "SELECT `h`.`id`, `h`.`name` AS `name`, `hb`.`num` AS `num`, `hs`.`category` AS `category` FROM (( `hotel` `h` LEFT JOIN `hotel_bed` `hb` ON ((`hb`.`hotel_id` = `h`.`id`))) LEFT JOIN `hotel_stock` `hs` ON ((`hs`.`hotel_id` = `h`.`id`)))";
			parse(sql);
		}
		{
			String sql = "SELECT `h`.`id`, `hs`.`hotel_id` as id, `h`.`name` AS `name`, `hb`.`num` AS `num`, `hs`.`category` AS `category` FROM (( `hotel` `h` LEFT JOIN `hotel_bed` `hb` ON ((`hb`.`hotel_id` = `h`.`id`))) LEFT JOIN `hotel_stock` `hs` ON ((`hs`.`hotel_id` = `h`.`id`)))";
			parse(sql);
		}
		

	}

	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void parse(String sql) {


		System.out.println(sql);
		String dbType = JdbcConstants.MYSQL;

		List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
		if (stmtList.size() != 1) {
			throw new RuntimeException("View Sql 解析异常");
		}
		
		SQLStatement stmt = stmtList.get(0);
		MySqlSchemaStatVisitor vt = new MySqlSchemaStatVisitor();
		stmt.accept(vt);

		// 1.获取所有表名，自动构建 表的数目
		Map<Name, TableStat> tables = vt.getTables();
		Set<Name> tableNames = tables.keySet();
		Map<String, String> aliasMap = vt.getAliasMap();
//		System.out.println("Tables : " + tableNames);
//		System.out.println("fields : " + vt.getColumns());
//		System.out.println("Alias  : " + aliasMap);
//		System.out.println();
		
		// 2.解析SQL，获取查询显示列 a.id = b.id and b.id = c.id
		List<String> selectItem = new ArrayList<>();
		SqlParse sp = new SqlParse(EovaConfig.EOVA_DBTYPE, sql);
		for (SQLSelectItem item : sp.getSelectItem()) {
			SQLPropertyExpr pe = (SQLPropertyExpr)item.getExpr();
			String name = pe.getName();
			String ow = pe.getOwner().toString();
			selectItem.add(aliasMap.get(ow) + '.' + name);
		}
		
		System.out.println("Show Field：" + selectItem);
		
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
				refs.put(left, set);//rs.getLeft().getTable()
			}
			{
				Set<String> set = refs.get(right);
				if (set == null) {
					set = new HashSet<>();
				}
				set.add(left);
				refs.put(right, set);// rs.getRight().getTable()
			}
			rs.getRight();
		}
		System.out.println("关系:" + refs);
		
		
		for(Name n : tableNames){
			String name = n.getName();
			System.out.println("table: " + name);
			
			Iterator iter = refs.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String)entry.getKey();
				Set<String> val = (Set<String>)entry.getValue();
				
				if (key.startsWith("`" + name +"`")) {
					System.out.println("whereField: " + DbUtil.getEndName(key));
					
					for (String field : val) {
						if (selectItem.contains(field)) {
							System.out.println("left-paramField: " + DbUtil.getEndName(field));
							break;
						} else {
							boolean flag = findRefField(selectItem, refs.get(field));
							if (flag) {
								break;
							}
						}
					}
				}	
			}
			
			System.out.println();
		}
	}
		
	private static boolean findRefField(List<String> selectItem, Set<String> val){
		for (String field : val) {
			if (selectItem.contains(field)) {
				System.out.println("right-paramField: " + DbUtil.getEndName(field));
				return true;
			}
		}
		return false;
	}
		
//			for (Relationship rs : relationships) {
//				String whereField = "";
//				if (compare(rs.getLeft().getTable(), name)) {
//					whereField = rs.getLeft().getName();
//				} else if (compare(rs.getRight().getTable(), name)) {
//					whereField = rs.getRight().getName();
//				}
//				if (whereField.equals("")) {
//					continue;
//				}
//				
//				System.out.println("whereField: " + whereField);
//				
//				String left = rs.getLeft().toString();
//				String right = rs.getRight().toString();
//				String paramField = "";
//				if (selectItem.contains(left)) {
//					paramField = rs.getLeft().getName();
//				} else if (selectItem.contains(right)) {
//					paramField = rs.getRight().getName();
//				}
//				System.out.println("paramField: " + paramField);
//				
//				System.out.println();
//				break;
//				
//			}

		
		// 然后根据关系，递归线性，往下查找 对应的字段（必须是查询显示列）
		
		
		
//		System.out.println("fields : " + vt.getColumns());
//		System.out.println("Alias  : " + vt.getAliasMap());
//		System.out.println("Conds  : " + vt.getConditions());

		
		// List<Condition> cds = visitor.getConditions();
		// for (Condition cd : cds) {
		// System.out.println(cd);
		// }

}


//String select = exp.simpleSelect.replace("select", "").replace("`", "").replace(" ", "");
//System.out.println(select);
//for (String item : select.split(",")) {
//	if (item.contains(".")) {
//		String[] ss = item.split("\\.");
//		item = ss[1];
//	}
//}

//for (Relationship rs : relationships) {
//	System.out.println(rs);
//	System.out.println("table: " + rs.getLeft().getTable());
//	System.out.println("where: " + rs.getLeft().getName());
//	String left = rs.getLeft().toString();
//	String right = rs.getRight().toString();
//	
//	String paramField = "";
//	if (selectItem.contains(left)) {
//		paramField = rs.getLeft().getName();
//	} else if (selectItem.contains(right)) {
//		paramField = rs.getRight().getName();
//	}
//	System.out.println("param: " + paramField);
//}