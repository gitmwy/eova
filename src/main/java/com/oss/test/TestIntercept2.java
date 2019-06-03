/**
 * 
 */
package com.oss.test;

import java.util.Date;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;

/**
 * mysql create_time 没法默认时间，我协助
 * @author jin
 *
 */
public class TestIntercept2 extends MetaObjectIntercept {
	
	 public String addBefore(AopContext ac) throws Exception {
		 //设置创建时间
		 ac.record.set("Create_time", new Date());
	        return null;
	    }
}
