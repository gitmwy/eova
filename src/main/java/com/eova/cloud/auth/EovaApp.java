/**
 * Copyright (c) 2013-2017, Jieven. All rights reserved.
 * <p/>
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.cloud.auth;

public class EovaApp {

	// 是否合法授权应用
	private boolean isAuth = false;

	// 应用ID
	private String id;
	// 应用密钥
	private String secret;
	// 应用版权
	private String copyright;

	// 应用域名
	private String domain;
	// 应用名称
	private String name;
	// 应用LOGO
	private String logo;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public boolean isAuth() {
		return isAuth;
	}

	public void setAuth(boolean isAuth) {
		this.isAuth = isAuth;
	}

}