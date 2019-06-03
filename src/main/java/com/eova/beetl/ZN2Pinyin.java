/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.beetl;

import org.beetl.core.Context;
import org.beetl.core.Function;

import com.eova.common.utils.xx;
import com.eova.common.utils.string.PinyinTool;
import com.eova.model.MetaField;
import com.eova.model.Widget;

/**
 * 中文转拼音
 * 
 * @author jin
 * @date 2014-5-23
 */
public class ZN2Pinyin implements Function {
	
	@Override
	public Object call(Object[] paras, Context ctx) {
		String str = paras[0].toString();
		
		return PinyinTool.getInstance().toPinYinLower(str);
	}
}