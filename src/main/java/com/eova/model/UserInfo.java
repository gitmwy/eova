/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.model;

import com.eova.common.base.BaseModel;
import com.eova.config.EovaConst;

/**
 * 真正的user 表，需要自己绑定
 * EovaConfig.mapping 添加UserInfo的 Model Mapping
 * @author jin
 *
 */
public class UserInfo extends BaseModel<UserInfo> {

	private static final long serialVersionUID = 1064291771401662738L;

	public static final UserInfo dao = new UserInfo();

	

}