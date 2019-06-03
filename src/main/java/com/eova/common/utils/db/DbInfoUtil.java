package com.eova.common.utils.db;

import java.sql.Connection;  
import java.sql.DatabaseMetaData;  
import java.sql.DriverManager;  
import java.sql.ResultSet;  
import java.sql.SQLException;  
import java.util.ArrayList;  
import java.util.HashMap;  
import java.util.List;  
import java.util.Map;  
import java.util.Properties;  
  
/** 
 *  
 * <p>Description: 获取数据库基本信息的工具类</p> 
 *  
 * @author qxl 
 * @date 2016年7月22日 下午1:00:34 
 */  
public class DbInfoUtil {  
      
	/** 
     * 根据数据库的连接参数，获取指定表的基本信息：字段名、字段类型、字段注释 
     * @param driver 数据库连接驱动 
     * @param url 数据库连接url 
     * @param user  数据库登陆用户名 
     * @param pwd 数据库登陆密码 
     * @param table 表名 
     * @return Map集合 
     */  
    public static List getTableInfo(String driver,String url,String user,String pwd,String table){  
        List result = new ArrayList();  
          
        Connection conn = null;       
        DatabaseMetaData dbmd = null;  
          
        try {  
            conn = getConnections(driver,url,user,pwd);  
              
            dbmd = conn.getMetaData();  
            ResultSet resultSet = dbmd.getTables(null, "%", table, new String[] { "TABLE" });  
              
            while (resultSet.next()) {  
                String tableName=resultSet.getString("TABLE_NAME");  
                System.out.println(tableName);  
                  
                if(tableName.equals(table)){  
                    ResultSet rs = conn.getMetaData().getColumns(null, null,tableName,null);  
  
                    while(rs.next()){  
                        //System.out.println("字段名："+rs.getString("COLUMN_NAME")+"--字段注释："+rs.getString("REMARKS")+"--字段数据类型："+rs.getString("TYPE_NAME"));  
                        Map map = new HashMap();  
                        String colName = rs.getString("COLUMN_NAME");  
                        map.put("code", colName);  
                          
                        String remarks = rs.getString("REMARKS");  
                        if(remarks == null || remarks.equals("")){  
                            remarks = colName;  
                        }  
                        map.put("name",remarks);  
                          
                        String dbType = rs.getString("TYPE_NAME");  
                        map.put("dbType",dbType);  
                          
                        map.put("valueType", changeDbType(dbType));  
                        result.add(map);  
                    }  
                }  
            }  
        } catch (SQLException e) {  
            e.printStackTrace();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }finally{  
            try {  
                conn.close();  
            } catch (SQLException e) {  
                e.printStackTrace();  
            }  
        }  
          
        return result;  
    }  
      
    private static String changeDbType(String dbType) {  
        dbType = dbType.toUpperCase();  
        switch(dbType){  
            case "VARCHAR":  
            case "VARCHAR2":  
            case "CHAR":  
                return "1";  
            case "NUMBER":  
            case "DECIMAL":  
                return "4";  
            case "INT":  
            case "SMALLINT":  
            case "INTEGER":  
                return "2";  
            case "BIGINT":  
                return "6";  
            case "DATETIME":  
            case "TIMESTAMP":  
            case "DATE":  
                return "7";  
            default:  
                return "1";  
        }  
    }  
  
    //获取连接  
    private static Connection getConnections(String driver,String url,String user,String pwd) throws Exception {  
        Connection conn = null;  
        try {  
            Properties props = new Properties();  
            props.put("remarksReporting", "true");  
            props.put("user", user);  
            props.put("password", pwd);  
            Class.forName(driver);  
            conn = DriverManager.getConnection(url, props);  
        } catch (Exception e) {  
            e.printStackTrace();  
            throw e;  
        }  
        return conn;  
    }  
      
    //其他数据库不需要这个方法 oracle和db2需要  
    private static String getSchema(Connection conn) throws Exception {  
        String schema;  
        schema = conn.getMetaData().getUserName();  
        if ((schema == null) || (schema.length() == 0)) {  
            throw new Exception("ORACLE数据库模式不允许为空");  
        }  
        return schema.toUpperCase().toString();  
  
    }  
  
    public static void main(String[] args) {  
          
        //这里是Oracle连接方法  
          
        String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";  
        String url = "jdbc:sqlserver://192.168.1.250:1433;DatabaseName=cfh";  
        String user = "sa";  
        String pwd = "000000";  
        //String table = "FZ_USER_T";  
        String table = "M_Students";  
   
        //mysql  
        /*  
        String driver = "com.mysql.jdbc.Driver"; 
        String user = "root"; 
        String pwd = "000000"; 
        String url = "jdbc:mysql://192.168.1.251:3306/eova163?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull"; 
        String table = "eova_log"; 
         */
          
//        List list = getTableInfo(driver,url,user,pwd,table);  
//        System.out.println(list.size());  
//        System.out.println(list);  
        
        msql();
    } 
    
    
    
    public static void msql()
    {
    try{
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
      String url="jdbc:sqlserver://192.168.1.250:1433;DatabaseName=cfh";
      String user="sa";
      String pass="000000";
      String s;
      Connection con=DriverManager.getConnection(url,user,pass);
          DatabaseMetaData dbmd=con.getMetaData();
      s = dbmd.getDriverName();
//      System.out.println("驱动程序的名称是: "+s);
//      System.out.println(" ");
//      s = dbmd.getDatabaseProductName();
//      System.out.println ("数据库名称是："+s);
//      System.out.println(" ");
      
      
      
      
      ResultSet rs = dbmd.getSchemas();
      System.out.println("模式名有：");
      while(rs.next())
        System.out.print("  "+rs.getString(1));
        System.out.println();
       
       s = dbmd.getSQLKeywords();
      System.out.println("SQL中的关键词为: "+s);
      System.out.println(" ");
      int max=dbmd.getMaxColumnNameLength();
      System.out.println ("列名的最大长度可以是："+max);
      System.out.println(" ");
      max = dbmd.getMaxTableNameLength();
      System.out.println ("表名的最大长度可以是："+max);
      System.out.println(" ");
      max = dbmd.getMaxColumnsInSelect();
      System.out.println ("一个select 子句所能返回的最多列数列名的最大长度可是是："+max);
      System.out.println(" ");
      max = dbmd.getMaxTablesInSelect();
      System.out.println ("一个SELECT语句最多可以访问多少个表："+max);
      System.out.println(" ");

      max = dbmd.getMaxColumnsInTable();
      System.out.println ("表中允许的最多列数："+max);
      System.out.println(" ");
      max = dbmd.getMaxConnections();
      System.out.println ("并发访问的用户个数："+max);
      System.out.println(" ");
      max = dbmd.getMaxStatementLength();
      System.out.println ("SQL语句最大允许的长度："+max);
      System.out.println(" ");
      s = dbmd.getNumericFunctions();
      System.out.println("数据库的所有数学函数的列表: "+s);
      System.out.println(" ");
      s = dbmd.getStringFunctions();
      System.out.println("数据库的所有字符串函数的列表: "+s);
      System.out.println(" ");
      s = dbmd.getSystemFunctions();
      System.out.println("数据库的所有系统函数的列表: "+s);
      System.out.println(" ");
      s = dbmd.getTimeDateFunctions();
      System.out.println("数据库的所有日期时间函数的列表: "+s);
      System.out.println(" ");
      rs = dbmd.getTypeInfo();
      while(rs.next())
        {
          System.out.print(" 数据类型名："+rs.getString(1));
          System.out.print("  数据类型："+ rs.getString(2));
          System.out.print("  精度："+   rs.getString(3));
          System.out.println("  基数："+   rs.getString(18));
        }
        System.out.println(" ");
      s = dbmd.getURL();
      System.out.println("此数据库的url: "+s);
      System.out.println(" ");
      s = dbmd.getUserName();
      System.out.println("此数据库的用户: "+s);
      System.out.println(" ");
      String [ ] t = { "TABLE", "VIEW" };
      rs = dbmd.getTables(null, "HR", "%", t);
      while(rs.next()){
        System.out.print("目录名："+rs.getString(1));
        System.out.print(" 模式名："+rs.getString(2));
        System.out.print(" 表名："+rs.getString(3));
        System.out.print(" 表的类型："+rs.getString(4));
        System.out.println(" 注释："+rs.getString(5));
        }
        System.out.println(" ");
      rs = dbmd.getPrimaryKeys(null, "HR","EMPLOYEES");
      while(rs.next()){
        System.out.print("目录名："+rs.getString(1));
        System.out.print(" 模式名："+rs.getString(2));
        System.out.print(" 表名："+rs.getString(3));
        System.out.print(" 列名顺序号："+rs.getString(4));
        System.out.print(" 列名顺序号："+rs.getString(5));
        System.out.println(" 主键名："+rs.getString(6));
        }
        System.out.println(" ");
       rs = dbmd.getTableTypes();
       System.out.println(" 表的类型有：");
       while(rs.next())
         System.out.print("  "+ rs.getString(1));
         System.out.println();
         System.out.println(" ");
         
        
       rs = dbmd.getColumns(null, null, "M_Students", "%");
       System.out.println(" 表名 "+" 列名 "+"  数据类型"+" 本地类型名"+" 列的大小"+ " 小数位数"+" 数据基数"+" 是否可空"+" 索引号");
       
       System.out.println("======================>");
       while(rs.next()){
//        System.out.print(rs.getString(3)+" ");
//        System.out.print(rs.getString(4)+" ");
//        System.out.print(rs.getString(5)+" ");
//        System.out.print(rs.getString(6)+" ");
//        System.out.print(rs.getString(7)+" ");
//        System.out.print(rs.getString(9)+" ");
//        System.out.print(rs.getString(10)+" ");
//        System.out.print(rs.getString(11)+" ");
//        System.out.println(rs.getString(17)+" ");
        
    	   int mx = rs.getRow();
    	   for(int x=1;x<=mx;x++)
    	   
    	   System.out.println(rs.getString(x)+" ");
        }
       System.out.println("<======================");
       
       
       
       
       
       
        System.out.println(" ");
        rs = dbmd.getIndexInfo(null, "HR", "EMPLOYEES", false, false);
         System.out.println(" 表名"+" 索引名"+" 索引类型"+" 索引列名"+" 索引顺序"+ " 小数位数"+" 数据基数"+" 是否可空"+" 索引号");
        while(rs.next()){
        System.out.print(rs.getString(3)+" ");
        System.out.print(rs.getString(6)+" ");
        System.out.print(rs.getString(7)+" ");
        System.out.print(rs.getString(9)+" ");
        System.out.println(rs.getString(10)+" ");
        }
        System.out.println(" ");
     
      rs.close();
         con.close();
      }
      catch(Exception e){System.out.println(e);}
      
    }
      
}  