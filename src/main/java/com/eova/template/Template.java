/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.template;

import java.util.List;
import java.util.Map;

import com.eova.model.Button;

/**
 * Eova 业务模版接口
 * 
 * @author Jieven
 * 
 */
public interface Template {
	/**
	 * 模版名称
	 * 
	 * @return
	 */
	String name();

	/**
	 * 模版编码
	 * 
	 * @return
	 */
	String code();

	/**
	 * 模版按钮组
	 * @return
	 */
	Map<Integer, List<Button>> getBtnMap();

}