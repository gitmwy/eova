/**
 * 
 */
package com.eova.interceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.alibaba.druid.util.StringUtils;
import com.eova.core.meta.MetaDataType;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author Administrator
 *
 */
public class MsqlInterceptor implements Interceptor {

	/* (non-Javadoc)
	 * @see com.jfinal.aop.Interceptor#intercept(com.jfinal.aop.Invocation)
	 */
	@Override
	public void intercept(Invocation inv) {
		// TODO Auto-generated method stub
		String objectCode = inv.getController().getPara(0);
		
		if(objectCode == null){
			inv.invoke();
			return;
		}
		
		System.out.println("==="+objectCode);
		MetaObject object = sm.meta.getMeta(objectCode);
		if(object == null){
			inv.invoke();
			return;
		}
		
		// 获取字段当前的值
		for (MetaField item : object.getFields()) {
			// System.out.println(item.getEn() +'='+ c.getPara(item.getEn()));
			String type = item.getStr("type");// 控件类型
			String key = item.getEn();// 字段名
			String data_type_name =(String) item.get("data_type_name");
			String pk = object.getPk();// 控件类型

			// 获当前字段 Requset Parameter Value，禁用=null,不填=""
			String value = inv.getController().getPara(key);
			
			
			
		  //if(isInsert){
				//mssql专用，uniqueidentifier 类型数据前段无视，本地补充
				if( "uniqueidentifier".equals(data_type_name) 
						&& StringUtils.isEmpty(value) 
						&& key.equalsIgnoreCase(pk) ){
					value = UUID.randomUUID().toString();
					//inv.getController().setAttr(name, value)
				}
			//}
				//其他待处理	
//				if(!object.getDs().equals("eova") ){
//					
//				}
		}
		
		inv.invoke();
	}

}
