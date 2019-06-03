/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eova.common.base.BaseModel;
import com.eova.common.utils.xx;
import com.eova.config.EovaConst;

/**
 * User=用户登录表，
 * UserInfo=用户信息表
 * 默认登录提取User即可，如果如果存在UserInfo 则UserInfo优先，UserInfo中的role也会覆盖User的rid（如果存在） 
 * ，User 和 UserInfo的关系？目前应该是id是一致的，所以用户管理在添加用户UserInfo以后 User也需要添加同ID的数据
 * 
 * 2、rid为原版本角色id，现调整为rids(逗号分隔) 以支持多个角色，同时第一个rid为主角色 用来显示用户角色，如沈校长 兼职 数学老师
 * @author jin
 *
 */
public class User extends BaseModel<User> {

	private static final long serialVersionUID = 1064291771401662738L;

	public static final User dao = new User();
	//用户自定义 用户表
	public UserInfo userInfo = null;
	private Role role;//第一角色
	
	private List<Role> roles;
	
	public List<String> getRids(){
		//return this.getInt("rid");
		String rids = null;
		if(userInfo != null && !xx.isEmpty(userInfo.getStr("rids")))
			rids = userInfo.getStr("rids");
		else
			rids = this.getStr("rids");
		
		if(!xx.isEmpty(rids) ){
			List<String> list = Arrays.asList(rids.split(","));
			return list;
		}else
			return new ArrayList();
			
		
	}
	
	/**
	 * 是否超级管理员
	 * @return
	 */
	public boolean isAdmin(){
		return getIsAdmin();
	}
	
	// 为兼容模版取值
	private boolean getIsAdmin(){
//		if (this.getRid() == EovaConst.ADMIN_RID) {
//			return true;
//		}
		List<String> rids = getRids();
			if(rids.contains(String.valueOf(EovaConst.ADMIN_RID)))
				return true;
		return false;
	}

	public void init() {
		this.roles = Role.dao.findByIds(getRids());
		
		this.role = Role.dao.findById(getRids().get(0));
	}

	public User getByLoginId(String loginId) {
		return this.findFirst("select * from eova_user where login_id = ?", loginId);
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
	public Role getRole() {
		return role;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

}