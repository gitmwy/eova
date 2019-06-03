package com.eova.engine;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.eova.config.EovaConfig;
import com.eova.engine.sql.TableSource;
import com.jfinal.plugin.activerecord.Record;

public class Test {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// SELECT
		// `a`.`id` AS `id`,
		// `a`.`memo` AS `memo`,
		// `a`.`status` AS `status`,
		// `a`.`create_time` AS `create_time`,
		// `m`.`name` AS `name`,
		// `m`.`card_no` AS `card_no`
		// `c`.`user_id` AS `user_id`
		// FROM
		// (
		// `award` `a`
		// LEFT JOIN `member` `m` ON ((`a`.`uid` = `m`.`id`))
		// LEFT JOIN `car` `c` ON ((`m`.`id` = `c`.`user_id`))
		// LEFT JOIN `car1` `c1` ON ((`m`.`id` = `c1`.`user_id`))
		// )

		// String sql =
		// "select `a`.`id` AS `id`,`a`.`memo` AS `memo`,`a`.`status` AS `status`,`a`.`create_time` AS `create_time`,`m`.`name` AS `name`,`m`.`card_no` AS `card_no` from (`award` `a` left join `member` `m` on((`a`.`uid` = `m`.`id`)))";
		// 左链接查询
		// String sql =
		// "select `c`.`user_name` AS `user_id`,`a`.`id` as `id`,`a`.`memo` AS `memo`,`a`.`status` AS `status`,`a`.`create_time` AS `create_time`,`m`.`name` AS `name`,`m`.`card_no` AS `card_no` from (`award` `a` left join `member` `m` on((`a`.`uid` = `m`.`id`))  LEFT JOIN `car` `c` ON ((`m`.`id` = `c`.`user_id`))  )";
		// 手写链接查询
		String sql = "select `a`.`id` AS `id`,`a`.`memo` AS `memo`,`a`.`status` AS `status`,`a`.`uid` AS `uid`,`m`.`card_no` AS `card_no`,`m`.`address` AS `address` from (`award` `a` join `member` `m`) where (`a`.`uid` = `m`.`id`)";
		sql = sql.replaceAll("`", "");
		{
			// 初始化SQL解析器
			SqlParse sp = new SqlParse(EovaConfig.EOVA_DBTYPE, sql);

			Map<String, Record> data = new HashMap<String, Record>();
			for (SQLSelectItem item : sp.getSelectItem()) {
				String en = SqlParse.getExprName(item.getExpr());
				String ow = SqlParse.getExprOw(item.getExpr());
				System.out.println("获取参数" + en + " 归属表：" + ow);

				Record r = null;
				if (data.containsKey(ow)) {
					r = data.get(ow);
				} else {
					r = new Record();
					data.put(ow, r);
				}
				r.set(en, "111");

			}
			// System.out.println(map.toString());
			System.out.println("------------------------------");

			SQLExpr where = sp.query.getWhere();
			System.out.println(where);

			SQLTableSource formTs = sp.query.getFrom();

			// LinkedHashMap<String, Map<String, String>> source = new LinkedHashMap<>();
			List<TableSource> sources = new ArrayList<TableSource>();

			// 递归计算保存
			SqlParse.parseTableSource(formTs, sources);

			// System.out.println(source.toString());

			for (TableSource ts : sources) {
				Record r = data.get(ts.getAlias());

				String table = ts.getTable();
				String field = ts.getRigthField();
				if (r.getColumns().containsKey(field)) {
					field = ts.getLeftField();
				}
				@SuppressWarnings("unused")
				Object value = r.get(field);

				// Db.update(tableName, primaryKey, record)

				System.out.println(MessageFormat.format("Db.save({0}, {1});update {2} set xxx = ? where {3} = ?;", table, r.toJson(), table, ts.getLeftField()));
				System.out.println();
			}

		}
	}
}
