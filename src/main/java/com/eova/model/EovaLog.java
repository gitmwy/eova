/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.model;

import java.util.List;

import com.eova.common.base.BaseModel;
import com.eova.common.utils.web.RequestUtil;
import com.eova.config.EovaConst;
import com.jfinal.core.Controller;

/**
 * 系统操作日志
 *
 * @author Jieven
 * @date 2014-9-10
 */
public class EovaLog extends BaseModel<EovaLog> {

	private static final long serialVersionUID = -1592533967096109392L;

	public static final EovaLog dao = new EovaLog();

	/** 新增 **/
	public static final int ADD = 1;
	/** 修改 **/
	public static final int UPDATE = 2;
	/** 删除 **/
	public static final int DELETE = 3;
	/** 导入 **/
	public static final int IMPORT = 4;
	/** 登陆 **/
	public static final int LOGIN = 5;
	/** 登出 **/
	public static final int LOGOUT = 6;
	
	/**
	 * 操作日志
	 * @param con
	 * @param info 日志详情
	 */
	public void info(Controller con, int type, String info){
		EovaLog el = new EovaLog();
		// TYPE
		el.set("type", type);
		// UID
		User user = con.getSessionAttr(EovaConst.USER);
		if(user != null)//用户可能未登陆的操作
			el.set("user_id", user.getInt("id"));
		// IP
		String ip = RequestUtil.getIp(con.getRequest());
		el.set("ip", ip);
		el.set("info", info);
		el.save();
	}
	
	
	
	public List<EovaLog> getUnInitLogs() {
		return this.find("select * from eova_log where city is null order by id desc");
	}
	

}