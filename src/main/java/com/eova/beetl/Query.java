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
import com.eova.model.MetaField;
import com.eova.model.Widget;

/**
 * 模版数据查询函数
 * 
 * @author Jieven
 * @date 2014-5-23
 */
public class Query implements Function {
	@Override
	public Object call(Object[] paras, Context ctx) {
		String type = paras[0].toString();
		if (type.equals("fields")) {
			// 元字段查询
			String objectCode = paras[1].toString();
			if (xx.isEmpty(objectCode) && !objectCode.matches("\\w+")) {
				throw new RuntimeException("参数异常，请输入合法参数");
			}
			return MetaField.dao.queryFieldsGroup(objectCode);
		} else if (type.equals("widget")) {
			// 自定义插件查询
			return Widget.dao.findByType(Widget.TYPE_DIY);
		}
		return null;
	}
}