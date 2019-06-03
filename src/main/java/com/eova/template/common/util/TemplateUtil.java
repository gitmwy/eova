/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.template.common.util;

import java.util.Date;

import com.eova.common.utils.xx;
import com.eova.common.utils.time.DateUtil;
import com.eova.common.utils.util.ExceptionUtil;
import com.eova.config.EovaConfig;
import com.eova.model.Button;
import com.eova.model.MetaField;

public class TemplateUtil {


	/**
	 * 值的类型转换
	 * @param item 元字段
	 * @param value
	 * @param safeMode 1=安全模式，0=否，-1=啥也不是（一般组建查询条件）
	 * @return
	 */
	public static Object convertValue(MetaField item, Object value,int safeMode) {
		// 控件类型
		String type = item.getStr("type");
		// 数据类型
		// String dataType = item.getDataTypeName();
		// 布尔框需要特转换值
		
		if (type.equals(MetaField.TYPE_BOOL)) {
			if(safeMode == 0){
				if(value == null){
					return null;
				}else{
					if (xx.isTrue(value)) {
						return 1;
					} else {
						return 0;
					}
				}
			}else {//1||-1
				if (xx.isTrue(value)) {
					return 1;
				} else {
					return 0;
				}
			}
		}else if(type.equals(MetaField.TYPE_TIME)){//时间类型推荐方案，DATETIME的由用户确定是否有值（有就有没有就没有把）TIMESTAMP则走下面的方案
			String data_type_name = item.get("data_type_name");//DATETIME/TIMESTAMP			
			if("".equals(value) && safeMode>=0 && "TIMESTAMP".equalsIgnoreCase(data_type_name)){//替你塞一个值，觉得你需要哈（主要对应的是 哪些审核时间，创建时间啥的需要默认给个值，要么设置数据库可以自动绑定，要么通过此方案）
				value = DateUtil.format(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS);
			}
		}else if(type.equals(MetaField.TYPE_DATE)){//暂时不管
		}
		
		//试卷特殊处理下，保持 yyyy-MM-dd HH:mm:ss
//		if (!xx.isEmpty(value) && type.equals(MetaField.TYPE_TIME)) {
//			return value;
//		}else if(!xx.isEmpty(value) && type.equals(MetaField.TYPE_DATE)){
//			return value+" 00:00:00";
//		}
	
		
		
		
		// Oracle Date格式化
//		if (xx.isOracle()) {
//			if (!xx.isEmpty(value)) {
//				if (type.equals(MetaField.TYPE_TIME)) {
//					value = java.sql.Timestamp.valueOf(value.toString());
//				} else if(type.equals(MetaField.TYPE_DATE)){
//					value = java.sql.Date.valueOf(value.toString());
//				}
//			}
//		}
		return value;
	}
	
	public static Object convertValue(MetaField item, Object value,String ds) {
		// 控件类型
		String type = item.getStr("type");
		// 数据类型
		// String dataType = item.getDataTypeName();
		// 布尔框需要特转换值
		if (type.equals(MetaField.TYPE_BOOL)) {
			if (xx.isTrue(value)) {
				return 1;
			} else {
				return 0;
			}
		}
		
		if(xx.isEmpty(ds) || ds.equalsIgnoreCase("eova") )
			ds = EovaConfig.EOVA_DBTYPE;
		else
			ds = EovaConfig.MAIN_DBTYPE;
		
		if(xx.isMssql(ds)){
			
		}
		
		// Oracle Date格式化
//		if (xx.isOracle()) {
//			if (!xx.isEmpty(value)) {
//				if (type.equals(MetaField.TYPE_TIME)) {
//					value = java.sql.Timestamp.valueOf(value.toString());
//				} else if(type.equals(MetaField.TYPE_DATE)){
//					value = java.sql.Date.valueOf(value.toString());
//				}
//			}
//		}
		return value;
	}

	/**
	 * 构建异常信息为HTML
	 * 
	 * @param e
	 * @return
	 */
	public static String buildException(Exception e) {
		e.printStackTrace();
		
		String type = e.getClass().getName();
		type = type.replace("java.lang.", "");
		return "<br/><p title=\"" + ExceptionUtil.getStackTrace(e) + "\">" + type + ":" + e.getMessage() + "</p>";
	}

	/**
	 * 初始化业务拦截器
	 * 
	 * @param bizIntercept
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T initIntercept(String bizIntercept) throws Exception {
		if (!xx.isEmpty(bizIntercept)) {
			try {
				// 实例化自定义拦截器
				return (T) Class.forName(bizIntercept).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("实例化业务拦截器异常，请检查类是否存在:" + bizIntercept);
			}
		}
		return null;
	}
	
	/**
	 * 默认查询按钮
	 * @return
	 */
	public static Button getQueryButton(){
		Button btn = new Button();
		btn.set("name", "查询");
		btn.set("ui", "query");
		return btn;
	}
}