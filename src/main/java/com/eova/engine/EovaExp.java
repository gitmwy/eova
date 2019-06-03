/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.engine;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.util.JdbcUtils;
import com.eova.common.utils.xx;
import com.eova.config.EovaConfig;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;

/**
 * Eova表达式解析器
 * 
 * @author Jieven
 * 
 */
public class EovaExp {

	public String ds;
	public String sql;
	public String select;
	public String simpleSelect;
	public String from;
	public String where;
	public String order;
	/**
	 * pk和 cn存在问题，比如select a.pk,a.cn,b.pk,b.cn 则输出值为pk以及cn，如果存在多表会生成sql有问题，需要a.pk,a.cn
	 * ，但是目前不知道影响有多大所以暂时未修改
	 */
	public String pk;
	public String cn;

	protected SqlParse sp;

	public EovaExp() {
	}
	
	/**
	 * @param exp
	 * @param isLoose是否松，如果松则去掉[]
	 */
	public EovaExp(String exp,boolean isLoose) {//宽松的，则需要去掉"[]"中的部分
		try {
			// 格式化
			exp = exp.trim().toLowerCase();
			
			//sql支持"[]",
			if(isLoose){//宽松的？则去掉"[]"中的部分
				if(exp.indexOf("[") != -1 && exp.indexOf("]") != -1)
					exp = exp.substring(0,exp.indexOf("[")-1) +" "+ exp.substring(exp.indexOf("]")+1);
			}else{
				exp = exp.replace("[", " ").replace("]", " ");
			}
			
			String dbType = EovaConfig.EOVA_DBTYPE;
			// 拆分表达式
			if (!exp.contains(";")) {
				this.sql = exp;
				this.ds = xx.DS_MAIN;
				dbType = JdbcUtils.SQL_SERVER;
			} else {
				String[] strs = exp.split(";");
				this.sql = strs[0];
				this.ds = strs[1].substring(3);
				// TODO 截取
				if(!this.ds.equals(xx.DS_EOVA))
					dbType = JdbcUtils.SQL_SERVER;
			}

			// 初始化SQL解析器
			sp = new SqlParse(dbType, sql);

			// 解析Sql
			initParse();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public EovaExp(String exp) {
		this(exp,false);//默认严格的
	}

	public static void main(String[] args) throws Exception {
		// 普通默认数据源
		print("select value ID,name CN from dicts where object = 'hotel' and field = 'state'",true);
		// 多字段排序
		print("select g.id ID,g.name 昵称 from game g where 1=1 and id = 3 order by id ,indexNum desc;ds=main",true);
		// 单字排序
		print("select id id,nickname cn from users where id > 1 order by id desc",true);
		// 子查询
		print("select id ID,name CN from xx where id in(select id from xxx)",true);
		// 多表联查带别名
		print("select a.id ID,a.name CN from a,b where a.id = b.aid and a.id > 1 order by a.id",true);
		// 多表联查带别名
		print("select user.id as myid,user.name,info.name,info.value,g.name,g.sc from user left join info on user.id = info.uid left join gift g on g.uid = user.id where user.id > 10",true);
	
		print("select a.id ID,a.name CN from a,b where a.id = b.aid [and a.id=3] and a.id > 1",true);
		print("select a.id ID,a.name CN from a,b where a.id = b.aid and a.id=3 [and a.id > 1]",false);
	}

	private static void print(String exp,boolean isLoose) {

		EovaExp se = new EovaExp(exp,isLoose);
		System.out.println(format(se.sql));
		System.out.println();
		System.out.println("DS:" + se.ds);
		System.out.println("SS:" + se.simpleSelect);
		System.out.println("SELECT:" + se.select);
		System.out.println("FROM:" + se.from);
		System.out.println("WHERE:" + se.where);
		System.out.println("ORDER:" + se.order);
		System.out.println("PK:" + se.pk);
		System.out.println("CN:" + se.cn);
		System.out.println();
		System.out.println();
		// SQLUtils.formatMySql(se.sql, new FormatOption(false));
	}

	public void initParse() {
		List<SQLSelectItem> items = sp.getSelectItem();
		try {
			// 第1列默认为主键
			this.pk = SqlParse.getExprName(items.get(0).getExpr());
			// 第2列默认为CN键
			this.cn = SqlParse.getExprName(items.get(1).getExpr());
		} catch (Exception e) {
			e.printStackTrace();
		}
		buildSelect();
		buildSimpleSelect();
		buildFrom();
		buildWhere();
		buildOrder();
	}

	private void buildSelect() {
		this.select = "select " + sp.getSelectItem().toString().replaceAll("\\[|\\]", "");
	}

	private void buildSimpleSelect() {
		StringBuilder sb = new StringBuilder("select ");
		for (SQLSelectItem item : sp.getSelectItem()) {
			sb.append(item.getExpr()).append(", ");
		}
		sb.delete(sb.length() - 2, sb.length() - 1);
		this.simpleSelect = sb.toString().trim();
	}

	private void buildFrom() {
		SQLTableSource ts = sp.query.getFrom();
		String alias = ts.getAlias();

		String s = " from " + ts.toString();
		if (alias != null) {
			s += ' ' + alias;
		}

		this.from = format(s);
	}

	private void buildWhere() {
		SQLExpr exp = sp.query.getWhere();
		if (exp == null) {
			this.where = "";
			return;
		}
		String s = " where " + SQLUtils.toSQLString(exp);
		this.where = format(s);
	}

	private void buildOrder() {
		StringBuilder sb = new StringBuilder(" order by ");

		List<SQLSelectOrderByItem> items = sp.getOrderItem();
		if (items == null) {
			return;
		}
		for (SQLSelectOrderByItem x : items) {
			// SQLIdentifierExpr exp = (SQLIdentifierExpr) x.getExpr();
			// sb.append(exp.getName());
			// if (x.getType() != null) {
			// sb.append(' ' + x.getType().name());
			// }
			sb.append(SQLUtils.toSQLString(x));
			sb.append(',');
		}
		sb.delete(sb.length() - 1, sb.length());

		this.order = sb.toString();
	}

	private static String format(String str) {
		// str = SQLUtils.format(str, EovaConfig.EOVA_DBTYPE, new FormatOption(false));
		str = str.replaceAll("\t|\r|\n", " ");
		str = str.replaceAll("  ", " ");
		return str;
	}

	/**
	 * 构建元对象
	 * 
	 * @param exp 表达式
	 * @return
	 */
	public MetaObject getObject() {
		// 获取元对象模版
		MetaObject eo = MetaObject.dao.getTemplate();
		eo.put("data_source", ds);
		eo.put("name", "");
		// 获取第一的值作为主键
		eo.put("pk_name", pk);
		// 获取第二列的值作为CN
		eo.put("cn", cn);

		return eo;
	}

	/**
	 * 构建元字段属性
	 * 
	 * @param exp 表达式
	 * @return
	 */
	public List<MetaField> getFields() {

		List<MetaField> fields = new ArrayList<MetaField>();
		int index = 0;
		List<SQLSelectItem> items = sp.getSelectItem();
		for (SQLSelectItem item : items) {
			index++;

			SQLIdentifierExpr expr = (SQLIdentifierExpr) item.getExpr();

			// 字段名->字段名
			String en = expr.getName();
			// 字段别名->字段列名
			String cn = item.getAlias();

			// 首列之后的默认都可以查询
			boolean isQuery = true;
			if (index == 1) {
				isQuery = false;
			}
			fields.add(buildItem(index, en, cn, isQuery));
		}
		return fields;
	}

	/**
	 * 手工组装字段元数据
	 * 
	 * @param index 排序
	 * @param en 英文名
	 * @param cn 中文名
	 * @param isQuery 是否可查询
	 * @return
	 */
	public static MetaField buildItem(int index, String en, String cn, boolean isQuery) {

		en = en.toLowerCase();
		// 获取元模版字段
		MetaField ei = MetaField.dao.getTemplate();
		ei.put("order_num", index);
		ei.put("en", en);
		ei.put("cn", cn);
		ei.put("type", "文本框");
		ei.put("is_query", isQuery);
		ei.put("width", 180);

		return ei;
	}

}