package com.eova.common.utils.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TxtUtil {

	 public static String getTxt(String path) {
	        File file = new File(path);
	        StringBuilder result = new StringBuilder();
	        InputStreamReader isr = null;
	        BufferedReader br = null;
	        try {
	           // BufferedReader br = new BufferedReader(new FileReader(file));// 构造一个BufferedReader类来读取文件
	            //文件读取会有乱码
	            isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
	            br = new BufferedReader(isr);
	           
	            String s = null;
	            while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
	                result.append(System.lineSeparator() + s);
	            }
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }finally{
	        	if(br!= null)
					try {
						br.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	        	if(isr!= null)
					try {
						isr.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        }
	        return result.toString();
	    }
}
