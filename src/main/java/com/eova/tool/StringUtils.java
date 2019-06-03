package com.eova.tool;

import java.util.UUID;

public class StringUtils {

	
	public static String getUUIDEnCode(String s){ 
        //去掉“-”符号 
        
		return s.substring(0,8)+"-//"+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24); 
    } 
	
	public static String setUUIDDeCode(String s){ 
        //加“-”符号 
        return s.substring(0,8)+s.substring(8,12)+s.substring(12,16)+s.substring(16,20)+s.substring(20); 
    } 
	
	public static String getUUID32(){
	    String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
	    return uuid;
	//  return UUID.randomUUID().toString().replace("-", "").toLowerCase();
	}
	
	public static void main(String[] args){
		System.out.println(getUUID32());
		System.out.println("格式前的UUID ： " + UUID.randomUUID().toString());
		System.out.println(setUUIDDeCode(UUID.randomUUID().toString()));
	}
}
