/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.core.button;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.model.RoleBtn;

public class ButtonIntercept extends MetaObjectIntercept {

	@Override
	public String deleteBefore(AopContext ac) throws Exception {
		int id = ac.record.getInt("id");

		// 删除菜单按钮关联权限
		RoleBtn.dao.deleteByBid(id);

		return null;
	}
	
	
	@Override
	public String updateBefore(AopContext ac) throws Exception {
		String ui =  ac.record.get("ui");
		
		//修改按钮路径了，如果非系统路径，则认为不是基本方法了
		///eova/template/single/btn/add.html
		//query
		if(ui != null && (ui.startsWith("/eova") || ui.equals("query"))){
			ac.record.set("is_base", 1);
		}else
			ac.record.set("is_base", 0);
			
	        return null;
	}
}