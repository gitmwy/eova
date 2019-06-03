package com.eova.common.utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.util.JdbcUtils;
import com.eova.config.EovaConfig;

/**
 * What：高频常用方法集合<br>
 * Where：xx.xxx简短快捷的输入操作<br>
 * Why：整合高频常用方法,编码速度+50%,代码量-70%
 * @author Jieven
 * 
 */
public class xx {
	
	/**默认数据源名称**/
	public static final String DS_MAIN = "main";
	/**EOVA数据源名称**/
	public static final String DS_EOVA = "eova";
	
	/**
	 * 对象是否为空
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object o) {
		if (o == null) {
			return true;
		}
		if (o instanceof String) {
			if (o.toString().trim().equals("")) {
				return true;
			}
		} else if (o instanceof List) {
			if (((List) o).size() == 0) {
				return true;
			}
		} else if (o instanceof Map) {
			if (((Map) o).size() == 0) {
				return true;
			}
		} else if (o instanceof Set) {
			if (((Set) o).size() == 0) {
				return true;
			}
		} else if (o instanceof Object[]) {
			if (((Object[]) o).length == 0) {
				return true;
			}
		} else if (o instanceof int[]) {
			if (((int[]) o).length == 0) {
				return true;
			}
		} else if (o instanceof long[]) {
			if (((long[]) o).length == 0) {
				return true;
			}
		} else if(o instanceof Integer) {
			if (o.toString().trim().equals("0")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 对象组中是否存在 Empty Object
	 * @param os 对象组
	 * @return
	 */
	public static boolean isOneEmpty(Object... os) {
		for (Object o : os) {
			if(isEmpty(o)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 对象组中是否全是 Empty Object
	 * @param os
	 * @return
	 */
	public static boolean isAllEmpty(Object... os) {
		for (Object o : os) {
			if (!isEmpty(o)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 是否为数字
	 * @param obj
	 * @return
	 */
	public static boolean isNum(Object obj) {
		try {
			Integer.parseInt(obj.toString());
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 字符串是否为 true
	 * @param str
	 * @return
	 */
	public static boolean isTrue(Object str) {
		if (isEmpty(str)) {
			return false;
		}
		str = str.toString().trim().toLowerCase();
		if (str.equals("true") || str.equals("on") || str.equals("1")) {
			return true;
		}
		return false;
	}

	/**
	 * 格式化字符串->'str'
	 * @param str
	 * @return
	 */
	public static String format(Object str) {
		return "'" + str.toString() + "'";
	}

	/**
	 * 强转->Integer
	 * @param obj
	 * @return
	 */
	public static Integer toInt(Object obj) {
		return Integer.parseInt(obj.toString());
	}

	/**
	 * 强转->Integer
	 * @param obj
	 * @param defaultValue
	 * @return
	 */
	public static Integer toInt(Object obj, int defaultValue) {
		if (isEmpty(obj)) {
			return defaultValue;
		}
		return toInt(obj);
	}

	/**
	 * 强转->Long
	 * @param obj
	 * @return
	 */
	public static long toLong(Object obj) {
		return Long.parseLong(obj.toString());
	}

	/**
	 * 强转->Long
	 * @param obj
	 * @param defaultValue
	 * @return
	 */
	public static long toLong(Object obj, long defaultValue) {
		if (isEmpty(obj)) {
			return defaultValue;
		}
		return toLong(obj);
	}

	/**
	 * 强转->Double
	 * @param obj
	 * @return
	 */
	public static double toDouble(Object obj) {
		return Double.parseDouble(obj.toString());
	}
	
	/**
	 * 强转->Boolean
	 * @param obj
	 * @return
	 */
	public static Boolean toBoolean(Object obj) {
		return Boolean.parseBoolean(obj.toString());
	}
	
	/**
	 * 强转->Boolean
	 * @param obj
	 * @param defaultValue
	 * @return
	 */
	public static Boolean toBoolean(Object obj, Boolean defaultValue) {
		if (isEmpty(obj)) {
			return defaultValue;
		}
		return toBoolean(obj);
	}
	
	/**
	 * 强转->java.util.Date
	 * @param str 日期字符串
	 * @return
	 */
	public static Date toDate(String str) {
		try {
			if (str == null || "".equals(str.trim()))
				return null;
			return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(str.trim());
		} catch (Exception e) {
			throw new RuntimeException("Can not parse the parameter \"" + str + "\" to Date value.");
		}
	}
	
	/**
	 * 是否Oracle数据源
	 * @return
	 */
	public static boolean isOracle(){
		return EovaConfig.EOVA_DBTYPE.equals((JdbcUtils.ORACLE));
	}
	
	public static boolean isOracle(String ds){
		if(DS_MAIN.equals(ds)){
			return EovaConfig.MAIN_DBTYPE.equals((JdbcUtils.ORACLE));
		}else{
			return EovaConfig.EOVA_DBTYPE.equals((JdbcUtils.ORACLE));
		}
	}
	
	/**
	 * 是否Mysql数据源
	 * @return
	 */
	public static boolean isMysql(){
		return EovaConfig.EOVA_DBTYPE.equals((JdbcUtils.MYSQL));
	}
	
	public static boolean isMysql(String ds){

		
		if(DS_MAIN.equals(ds)){
			return EovaConfig.MAIN_DBTYPE.equals((JdbcUtils.MYSQL));
		}else{
			return EovaConfig.EOVA_DBTYPE.equals((JdbcUtils.MYSQL));
		}
	}
	
	public static boolean isMssql(){
		return EovaConfig.EOVA_DBTYPE.equals((JdbcUtils.SQL_SERVER));
	}
	
	public static boolean isMssql(String ds){
		if(DS_MAIN.equals(ds)){
			return EovaConfig.MAIN_DBTYPE.equals((JdbcUtils.SQL_SERVER));
		}else{
			return EovaConfig.EOVA_DBTYPE.equals((JdbcUtils.SQL_SERVER));
		}
		
	}
	
	/**
	 * Array转字符串(用指定符号分割)
	 * @param array
	 * @param sign
	 * @return
	 */
	public static String join(Object[] array, char sign) {
		if (array == null) {
			return null;
		}
		int arraySize = array.length;
		int bufSize = (arraySize == 0 ? 0 : ((array[0] == null ? 16 : array[0].toString().length()) + 1) * arraySize);
		StringBuilder sb = new StringBuilder(bufSize);

		for (int i = 0; i < arraySize; i++) {
			if (i > 0) {
				sb.append(sign);
			}
			if (array[i] != null) {
				sb.append(array[i]);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 删除末尾字符串
	 * @param str 待处理字符串
	 * @param sign 需要删除的符号
	 * @return
	 */
	public static String delEnd(String str, String sign){
		if (str.endsWith(sign)) {
			return str.substring(0, str.lastIndexOf(sign));
		}
		return str;
	}

	/**
	 * 获取配置项
	 * @param key
	 * @return
	 */
	public static String getConfig(String key){
		String value = EovaConfig.getProps().get(key);
		if (value == null) {
			return "";
		}
		return value;
	}
	
	/**
	 * 获取配置项
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getConfig(String key, String defaultValue){
		String value = EovaConfig.getProps().get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
	
	/**
	 * 获取Bool配置
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static boolean getConfigBool(String key, boolean defaultValue){
		return toBoolean(getConfig(key), defaultValue);
	}
	
	/**
	 * 获取Int配置
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static int getConfigInt(String key, int defaultValue){
		return toInt(getConfig(key), defaultValue);
	}
	
	/**
	 * 消耗毫秒数
	 * @param time
	 */
	public static void costTime(long time) {
		System.err.println("Load Cost Time:" + (System.currentTimeMillis() - time) + "ms\n");
	}
	
	/**
	 * 格式化输出JSON
	 * @param json
	 * @return
	 */
	public static String formatJson(String json) {
		int level = 0;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < json.length(); i++) {
			char c = json.charAt(i);
			if (level > 0 && '\n' == sb.charAt(sb.length() - 1)) {
				sb.append(getLevelStr(level));
			}
			switch (c) {
			case '{':
			case '[':
				sb.append(c + "\n");
				level++;
				break;
			case ',':
				sb.append(c + "\n");
				break;
			case '}':
			case ']':
				sb.append("\n");
				level--;
				sb.append(getLevelStr(level));
				sb.append(c);
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}
    
    private static String getLevelStr(int level){
        StringBuffer levelStr = new StringBuffer();
        for(int levelI = 0;levelI<level ; levelI++){
            levelStr.append("  ");
        }
        return levelStr.toString();
    }

}