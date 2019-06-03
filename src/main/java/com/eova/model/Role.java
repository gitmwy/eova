/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.model;

import java.util.List;

import com.eova.common.base.BaseModel;
import com.eova.common.utils.db.DbUtil;

/**
 * 用户角色
 *
 * @author Jieven
 * @date 2014-9-10
 */
public class Role extends BaseModel<Role> {

	private static final long serialVersionUID = -1794335434198017392L;

	public static final Role dao = new Role();
	
	/**
	 * 获取下级角色
	 * @param lv
	 * @return
	 */
	public List<Role> findSubRole(int lv){
		return this.find("select * from eova_role where lv > ?", lv);
	}
	
	public List<Role> findByIds(List<String> roleids){
		StringBuilder ret = new StringBuilder();
		DbUtil.joinIds(roleids, ret); 
		return this.find("select * from eova_role where id in "+ret.toString());
	}

}