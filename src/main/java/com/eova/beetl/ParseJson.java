/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.beetl;

import org.beetl.core.Context;
import org.beetl.core.Function;

import com.alibaba.fastjson.JSONObject;

/**
 * JSON字符串转对象
 * 
 * @author Jieven
 * @date 2014-5-23
 */
public class ParseJson implements Function {
	@Override
	public Object call(Object[] paras, Context ctx) {
		if (paras.length != 1) {
			throw new RuntimeException("参数错误，请传入一个JSON字符串");
		}
		Object para = paras[0];
		if (para == null) {
			return null;
		}
		return JSONObject.parse(para.toString());
	}
}