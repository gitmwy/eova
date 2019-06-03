package com.eova.engine;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.stat.TableStat.Name;
import com.alibaba.druid.stat.TableStat.Relationship;
import com.alibaba.druid.util.JdbcConstants;

/**
 * SQL 数据权限解析
 * @author Administrator
 *
 */
public class SQLTest {
	public static void main(String[] args) {
		 
        // String sql = "update t set name = 'x' where id < 100 limit 10";
        // String sql = "SELECT ID, NAME, AGE FROM USER WHERE ID = ? limit 2";
        // String sql = "select * from tablename limit 10";
 
//		{
//			String sql = "update user set name = 'x' where id < 100 limit 10";
//	        parse(sql);
//		}
//		{
//			String sql = "SELECT ID, NAME, AGE FROM USER WHERE ID = ? limit 2";
//			parse(sql);
//		}
//		{
//			String sql = "select * from user left join info on user.id = info.uid where user.id > 10";
//			parse(sql);
//		}
//		{
//			String sql = "select * from user,userinfo where user.id = userinfo.uid";
//			parse(sql);
//		}
		{
			String sql = "select user.id as myid,user.name,info.name,info.value,g.name,g.sc from user left join info on user.id = info.uid left join gift g on g.uid = user.id where user.id > 10 and g.sc in (select c.id from c)";
			parse(sql);
		}
 
    }

	private static void parse(String sql) {
		System.out.println(sql);
		String dbType = JdbcConstants.MYSQL;
 
        //格式化输出
//        String result = SQLUtils.format(sql, dbType);
//        System.out.println(result); // 缺省大写格式
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
 
        //解析出的独立语句的个数
//        System.out.println("size is:" + stmtList.size());
        for (int i = 0; i < stmtList.size(); i++) {
 
            SQLStatement stmt = stmtList.get(i);
            MySqlSchemaStatVisitor vt = new MySqlSchemaStatVisitor();
            stmt.accept(vt);
            
            Map<Name, TableStat> tables = vt.getTables();

            //获取表名称
//            System.out.println("Tables : " + visitor.getCurrentTable());
            //获取操作方法名称
            System.out.println("Tables : " + tables);
            //获取字段名称
            System.out.println("fields : " + vt.getColumns());
            System.out.println("Alias  : " + vt.getAliasMap());
            System.out.println("Conds  : " + vt.getConditions());
            System.out.println("Ref    : " + vt.getRelationships());
            
            System.out.println(vt.getRelationships());
            Set<Relationship> relationships = vt.getRelationships();
            for (Relationship relationship : relationships) {
				System.out.println(relationship);
			}
//            List<Condition> cds = visitor.getConditions();
//            for (Condition cd : cds) {
//            	System.out.println(cd);
//			}
            
        }
        System.out.println();
	}
}
