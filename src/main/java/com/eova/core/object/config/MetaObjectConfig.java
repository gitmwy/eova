/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.core.object.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.eova.common.utils.xx;
import com.eova.common.utils.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 元对象配置
 * 
 * @author Jieven
 * 
 */
public class MetaObjectConfig {
	//config本身(属性全是顺序的gson)
//	private JsonObject config;
	private Map config;
	
	//private LinkedHashMap config2;



	// 视图配置
	private LinkedHashMap<String, TableConfig> view;
	

	

	public MetaObjectConfig() {
	}
	

	public MetaObjectConfig(String s) {
		
		//config = (JsonObject) new JsonParser().parse(s);  
		
		this. config = GsonUtil.GsonToMaps(s);
		
		
		if(this.config != null) {
			Iterator entries = config.entrySet().iterator(); 
			while (entries.hasNext()) { 
			  Map.Entry entry = (Map.Entry) entries.next(); 
			  String key = (String)entry.getKey(); 
			  entry.getValue(); 
			  //System.out.println("Key = " + key + ", Value = " + value);
			  
			  if(key.toString().toLowerCase().endsWith("form")) {
					Object o = this. config.get(key.toString());
					 if(!(o instanceof Map)) {
						 Map oneForm = new LinkedHashMap();
						 config.put(key, oneForm);
						 oneForm.put("force", 0);
						 //oneForm.put("list", this. config.get(key.toString())); 
						 List l = new ArrayList();
								 l.addAll((List)o);
						 oneForm.put("list", l);
					 }
				}
			}
//			for (Object key : config.keySet()) { 
//				if(key.toString().toLowerCase().endsWith("form")) {
//					//Object  this. config.get(key.toString());
//					 if(!(this. config.get(key.toString()) instanceof Map)) {
//						 Map oneForm = new HashMap();
//						 config.put(key, oneForm);
//						 oneForm.put("force", 0);
//						 oneForm.put("list", this. config.get(key.toString()));
//					 }
//				}
//			  //System.out.println("Key = " + key); 
//			} 
		}
		
		if(config.get("view") != null)
			this.view = JSON.parseObject(GsonUtil.GsonString(config.get("view")), new TypeReference<LinkedHashMap<String, TableConfig>>() {});
	}
	

	public LinkedHashMap<String, TableConfig> getView() {
		return view;
	}

	public void setView(LinkedHashMap<String, TableConfig> view) {
		this.view = view;
	}
	public Map getConfig() {
		return config;
	}

	
	
	
	public static void main(String[] args) {
		MetaObjectConfig o = new MetaObjectConfig();
		// {"viewMap":{"users":{"key":"id","value":"id"},"users_exp":{"key":"users_id","value":"id"}}}
		LinkedHashMap<String, TableConfig> v = new LinkedHashMap<>();
		{
			TableConfig tc = new TableConfig();
			tc.setWhereField("id");
			tc.setParamField("id");
			v.put("users", tc);
		}
		{
			TableConfig tc = new TableConfig();
			tc.setWhereField("users_id");
			tc.setParamField("id");
			v.put("users_exp", tc);
			
			
		}
		o.setView(v);
		
		Map newOne = new HashMap();
		
		
		List aa = new ArrayList();
		aa.add("a");
		aa.add("b");
		newOne.put("view", aa);

		String s = JSONObject.toJSONString(o);
		System.out.println(xx.formatJson(s));
		
		LinkedHashMap<String, TableConfig> view = JSON.parseObject(s, new TypeReference<LinkedHashMap<String, TableConfig>>() {});
		
		MetaObjectConfig metaObjectConfig = new MetaObjectConfig(s);
		//String s1 = JSONObject.toJSOprivate JSONObject config;NString(metaObjectConfig);
		System.out.println(xx.formatJson(metaObjectConfig.getView().toString()));
		
		
      
               // LinkedHashMap t = gson.fromJson(s, LinkedHashMap.class);
		 s = JSONObject.toJSONString(newOne);
                Map t = GsonUtil.GsonToMaps(s);
                if(t.get("view") instanceof Map) {
                	System.out.println("map");
                }
                System.out.println(t);
		//Object o2 = metaObjectConfig.getConfig().getAsJsonObject("view").getAsJsonObject("users").get("whereField");
		//System.out.println(o2);
                
                
	}


}
