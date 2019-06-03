package com.eova.common.utils.db;

import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class DbUtil {

	/**
	 * 转换Oracle数据类型
	 *
	 * @param typeName DB数据类型
	 * @return
	 */
	private static String convertDataType(String typeName) {
		if (typeName.contains("INT")) {
			return "NUMBER";
		} else if (typeName.contains("BIT")) {
			return "CHAR";
		} else if (typeName.indexOf("TIME") != -1 || typeName.indexOf("DATE") != -1) {
			return "DATE";
		} else {
			return "VARCHAR2";
		}
	}

	public static void createOracleSql(String ds, String tableNamePattern) {

		StringBuilder sbs = new StringBuilder();
		StringBuilder sbDrop = new StringBuilder();
		StringBuilder sbDropSeq = new StringBuilder();
		StringBuilder sbCreateSeq = new StringBuilder();

		List<String> tables = DsUtil.getTableNamesByConfigName(ds, DsUtil.TABLE, null, tableNamePattern);

		for (String table : tables) {

			String pk = DsUtil.getPkName(ds, table);

			String drop = "drop table " + table + ";\n";
			sbDrop.append(drop);

			String dropSeq = "drop sequence seq_" + table + ";\n";
			sbDropSeq.append(dropSeq);

			// 获取表中最大值
			String sql = "select max("+ pk +") from " + table;
			Object max = Db.use(ds).queryColumn(sql);
			if (xx.isEmpty(max)) {
				max = 0;
			}

			String createSeq = "create sequence seq_" + table + " increment by 1 start with "+ max + 1 +" maxvalue 9999999999;\n";
			sbCreateSeq.append(createSeq);

			JSONArray list = DsUtil.getColumnInfoByConfigName(ds, table);

			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			StringBuilder sb3 = new StringBuilder();

			// System.out.println(list);

			sb.append("create table " + table);
			sb.append("(\n");

			for (int i = 0; i < list.size(); i++) {
	            JSONObject o = list.getJSONObject(i);

	            Record re = new Record();
				re.set("en", o.getString("COLUMN_NAME"));
				re.set("cn", o.getString("REMARKS"));
	            re.set("order_num", o.getIntValue("ORDINAL_POSITION"));
				re.set("is_required", "YES".equalsIgnoreCase(o.getString("IS_NULLABLE")) ? false : true);

				// 是否自增
				boolean isAuto = "YES".equalsIgnoreCase(o.getString("IS_AUTOINCREMENT")) ? true : false;
				re.set("is_auto", isAuto);
				// 字段类型
				String typeName = o.getString("TYPE_NAME");
				re.set("data_type", convertDataType(typeName));
				// 字段长度
				int size = o.getIntValue("COLUMN_SIZE");
				if(size == 0){
					size = 1;
				} else if(size  > 4000){
					size = 4000;
				}
				// 默认值
				String def = o.getString("COLUMN_DEF");
				re.set("defaulter", def);

				String dataType = re.getStr("data_type");
				// create table
				sb.append("    " + re.getStr("en") + " " + dataType + (dataType.equals("DATE") ? "" : "(" + size + ")"));
				if (re.getBoolean("is_required")) {
					sb.append(" NOT NULL");
				}
				sb.append(",\n");

				// create remarks
				String remarks = o.getString("REMARKS");
				if (!xx.isEmpty(remarks)) {
					String str = "comment on column %s.%s is '%s';\n";
					sb2.append(String.format(str, table, re.getStr("en"), remarks));
				}

				// add default
				{
					if (!xx.isEmpty(def)) {
						String str = "alter table %s modify %s default %s;\n";
						if (def.equals("CURRENT_TIMESTAMP")) {
							sb3.append(String.format(str, table, re.getStr("en"), "sysdate"));
						} else {
							sb3.append(String.format(str, table, re.getStr("en"), xx.format(def)));
						}
					}

				}

	        }
			sb.delete(sb.length() - 2, sb.length() - 1);
			sb.append(");\n");

			// 导入元字段
			// importMetaField(code, list);

			// 导入视图默认第一列为主键
			String pkName = DsUtil.getPkName(ds, table);
			if (!xx.isEmpty(pkName)) {
				String str = "\nalter table %s add constraint pk_%s primary key(%s);\n";
				sb2.insert(0, String.format(str, table, table, pkName));
			}

			// 导入元对象
			// importMetaObject(ds, type, table, name, code, pkName);

			sbs.append(sb);
			sbs.append(sb2);
			sbs.append(sb3);
			sbs.append("\n");
		}

		System.out.println(sbDrop.toString());
		System.out.println(sbDropSeq.toString());
		System.out.println(sbCreateSeq.toString());
		System.out.println(sbs.toString());
	}

	/**
	 * 对Oracle部分类型做强转
	 * @param value
	 * @param type
	 * @return
	 */
	public static Object convertOracleValue(Object value, int type){
		String s = value.toString();
		if (type == Types.NUMERIC) {
			if (s.contains(".")) {
				return Double.valueOf(s);
			}
			if (s.length() > 11) {
				return Long.valueOf(s);
			}
			if (s.length() <= 11) {
				return Integer.valueOf(s);
			}
		}
		if (type == Types.CHAR && s.length() == 1) {
			if (s.equals("1")) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}
		return value;
	}

	/**
	 * 格式化Oracle Date
	 * @param value
	 * @return
	 */
	public static String buildDateValue(Object value){
		return "to_date('"+ value +"','yyyy-mm-dd HH24:MI:SS')";
	}

	/**
	 * 将数据变成Mysql插入脚本
	 * @param list 待生成数据集
	 * @param table 表名
	 * @param auto 自增列
	 * @param sb
	 */
	public static void generateSql(List<Record> list, String table, String auto, StringBuilder sb) {
		Set<String> updatePid = new HashSet<>();
		for (Record r : list) {
			appendSql(table, auto, sb, r);
			if (table.equals("eova_menu")) {
				Integer pid = r.getInt("parent_id");
				if (pid == 0) {
					continue;
				}
				// 查找当前父的Code
				String findPCode = "select code from eova_menu where id = ?";
				String pcode = Db.use(xx.DS_EOVA).queryStr(findPCode, pid);
				// 迁移后自动按Code 更新关系
				String sql = String.format("UPDATE eova_menu SET parent_id = ( SELECT id FROM ( SELECT id FROM eova_menu WHERE CODE = '%s' ) m ) WHERE CODE = '%s'", pcode,  r.getStr("code"));
				updatePid.add(sql);
			}
		}

		sb.append("\n");

		for (String s : updatePid) {
			sb.append(s);
			sb.append(";\n");
		}
	}

	private static void appendSql(String table, String auto, StringBuilder sb, Record r) {
		sb.append("INSERT INTO "+ table +" (");
		String[] names = r.getColumnNames();
		for (String n : names) {
			if (n.equals(auto)) {
				continue;
			}
			sb.append(format(n));
			sb.append(',');
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")  VALUES (");
		for (String n : names) {
			if (n.equals(auto)) {
				continue;
			}
			sb.append(formatVal(r.get(n)));
			sb.append(',');
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(");");
		sb.append("\n");
	}

	public static String format(String name){
		return '`' + name + '`';
	}

	private static Object formatVal(Object o){
		if (o == null) {
			return o;
		}
		String s = o.toString();
		if (s.equals("true")) {
			return 1;
		}
		if (s.equals("false")) {
			return 0;
		}
		if (s.indexOf("'") != -1) {
			// 自动转义引号
			s = s.replaceAll("'", "\\\\'");
		}
		return xx.format(s);
	}

	/**
	 * 格式化SQL去除各种拼接逻辑产生的无意义字符
	 * @param sql
	 */
	public static String formatSql(String sql) {
		// 多个空格替换
		sql = sql.trim();
		sql = sql.replaceAll("\\s+", " ");
		if (sql.endsWith(" where 1=1")) {
			sql = sql.replace(" where 1=1", "");
		}
		sql = sql.replace(" where 1=1 and ", " where ");
		sql = sql.replace(" where 1=1 order ", " order ");
		return sql;
	}

	/**
	 * SQL表名比较,自动忽略符号,空格
	 * @param full 全称 db.table.id
	 * @param table 简称 db.table
	 * @return
	 */
	public static boolean compareTable(String full, String tableName) {
		full = full.replace("`", "").replace(" ", "").toLowerCase();
		tableName = tableName.replace("`", "").replace(" ", "").toLowerCase();

		// 格式:table vs table
		if (full.equals(tableName)) {
			return true;
		}

		String[] ss = full.split("\\.");
		// 格式:table.filed vs table
		if (ss.length == 2) {
			return ss[0].equals(tableName);
		}
		// 格式:db.table.filed vs db.table
		if (ss.length == 3) {
			return (ss[0] + "." + ss[1]).equals(tableName);
		}

		return false;
	}

	/**
	 * SQL字段比较,自动忽略符号,空格
	 * @param full 全称 db.table.filed
	 * @param table 简称 db.table
	 * @return
	 */
	public static boolean compareField(String full, String fieldName) {
		full = full.replace("`", "").replace(" ", "").toLowerCase();
		fieldName = fieldName.replace("`", "").replace(" ", "").toLowerCase();

		// 格式:filed vs filed
		if (full.equals(fieldName)) {
			return true;
		}

		String[] ss = full.split("\\.");
		// 格式:table.filed vs filed
		if (ss.length == 2) {
			return ss[1].equals(fieldName);
		}
		// 格式:db.table.filed vs table.filed
		if (ss.length == 3) {
			return (ss[1] + "." + ss[2]).equals(fieldName) || ss[2].equals(fieldName);
		}

		return false;
	}

	public static void main(String[] args) {
//		{
//			boolean s = compare("`demo`.`hotel_stock`.`hotel_id`", "demo.hotel");
//			System.out.println(s);
//		}
		{
			boolean s = compareTable("`demo`.`hotel`.`id`", "demo.hotel");
			System.out.println(s);
		}
	}

	/**
	 * 获取字段名
	 * 例：`hotel`.`id`
	 * 值：id
	 * @param full 库名.表名.字段名
	 * @return
	 */
	public static String getEndName(String full) {
		full = full.replace("`", "").replace(" ", "");

		if (full.contains(".")) {
			String[] ss = full.split("\\.");
			return ss[ss.length-1];
		}
		return full;
	}

	public static String simplify(String full) {
		full = full.replace("`", "").replace(" ", "");
		return null;
	}
	
	//将 id 列表 join 起来，用逗号分隔，并且用小括号括起来
	 public static void joinIds(List idList, StringBuilder ret) {
	  ret.append("(");
	  boolean isFirst = true;
	  for (Object id : idList) {
	    if (isFirst) {
	      isFirst = false;
	    } else {
	      ret.append(", ");
	    }
	    ret.append(id.toString());
	  }
	  ret.append(")");
	 }
	 
	 public static String joinIds(List idList ) {
		 StringBuilder ret = new StringBuilder();
		  ret.append("(");
		  boolean isFirst = true;
		  for (Object id : idList) {
		    if (isFirst) {
		      isFirst = false;
		    } else {
		      ret.append(", ");
		    }
		    ret.append(id.toString());
		  }
		  ret.append(")");
		  return ret.toString();
	 }

}
