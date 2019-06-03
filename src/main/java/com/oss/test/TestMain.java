/**
 * 
 */
package com.oss.test;

import java.util.LinkedHashMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eova.core.object.config.TableConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author jin
 *
 */
public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 JSONObject config;
		



	String s = "";
	JSONObject jsonObj =new JSONObject(new LinkedHashMap());
			
				//this. config = JSON.parseObject(s);
	
	String jsonStr = "{\"JACKIE_ZHANG\":\"张学友\",\"ANDY_LAU\":\"刘德华\",\"LIMING\":\"黎明\",\"Aaron_Kwok\":\"郭富城\"}" ;  
	  
	  
    //做5次测试  
    for(int i=0,j=5;i<j;i++)  
    {  
       JSONObject jsonObject = JSONObject.parseObject(jsonStr) ;  
       for(java.util.Map.Entry<String,Object> entry:jsonObject.entrySet()){  
           System.out.print(entry.getKey()+"-"+entry.getValue()+"\t");  
       }  
        System.out.println();//用来换行  
    }  
    
    DbPro db = new DbPro();
    Record data = null;
    		 ;
    db.update("", (Object)data.get("a"));
    
    //做5次测试  
    for(int i=0,j=5;i<j;i++)  
    {  
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);  
        for(java.util.Map.Entry<String,JsonElement> entry:jsonObject.entrySet()){  
            System.out.print(entry.getKey()+"-"+entry.getValue()+"\t");  
        }  
        System.out.println();//用来换行  
    }  
    
	}

}
