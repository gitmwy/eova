/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.interceptor;

import java.util.Set;

import com.eova.common.utils.xx;
import com.eova.common.utils.util.AntPathMatcher;
import com.eova.config.EovaConfig;
import com.eova.config.EovaConst;
import com.eova.model.User;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * 权限验证
 * 
 * @author Jieven
 * @date 2014-9-18
 */
public class AuthInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		String uri = inv.getController().getRequest().getRequestURI();
		AntPathMatcher pm = new AntPathMatcher();
		
		// 根目录功能+登录页免鉴权
		if (pm.match("/*", uri) || uri.endsWith("/toLogin")) {
			inv.invoke();
			return;
		}
		
		// 非登录免鉴权
		for (String pattern : LoginInterceptor.excludes) {
			if (pm.match(pattern, uri)) {
				inv.invoke();
				return;
			}
		}
				
		User user = inv.getController().getSessionAttr(EovaConst.USER);
		// 上帝无所不能
		if (user.isAdmin()) {
			inv.invoke();
			return;
		}
		
		String msg = "对不起，您没有权限进行该操作：" + uri;
		
		// 当前角色分配授权
		Set<String> authUriPattern = user.get("auths");
		if (xx.isEmpty(authUriPattern)) {
			inv.getController().renderText(msg);
			return;
		}
		// 当前角色自定义授权
//		Set<String> temp = EovaConfig.getAuths().get(user.getRid());
//		if (!xx.isEmpty(temp)) {
//			authUriPattern.addAll(temp);
//		}
		// 所有角色公共授权
		Set<String> temp = EovaConfig.getAuths().get(0);
		if(!xx.isEmpty(temp)){
			authUriPattern.addAll(temp);
		}
		// 检查授权
		for (String pattern : authUriPattern) {
			if (pm.match(pattern, uri)) {
				inv.invoke();
				return;
			}
		}
		
		inv.getController().renderText(msg);
	}

}