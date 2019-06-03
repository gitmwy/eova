/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.core.menu.config;

/**
 * 菜单表树配置
 *
 * @author Jieven
 *
 */
public class TreeConfig {

	// 对象编码
	private String objectCode;
	// 外键字段
	private String objectField;

	// 树形字段
	private String treeField;
	// ID字段
	private String idField;
	// 父ID字段
	private String parentField;
	// 根节点的值
	private String rootPid = "0";
	// 图标字段
	private String iconField;
	//子节点名（默认children）
	private String childrenField = "children";

	public String getIconField() {
		return iconField;
	}

	public void setIconField(String iconField) {
		this.iconField = iconField;
	}

	public String getTreeField() {
		return treeField;
	}

	public void setTreeField(String treeField) {
		this.treeField = treeField;
	}

	public String getParentField() {
		return parentField;
	}

	public void setParentField(String parentField) {
		this.parentField = parentField;
	}

	public String getIdField() {
		return idField;
	}

	public void setIdField(String idField) {
		this.idField = idField;
	}

	public String getObjectCode() {
		return objectCode;
	}

	public void setObjectCode(String objectCode) {
		this.objectCode = objectCode;
	}

	public String getObjectField() {
		return objectField;
	}

	public void setObjectField(String objectField) {
		this.objectField = objectField;
	}

	public String getRootPid() {
		return rootPid;
	}

	public void setRootPid(String rootPid) {
		this.rootPid = rootPid;
	}

	public String getChildrenField() {
		return childrenField;
	}

	public void setChildrenField(String childrenField) {
		this.childrenField = childrenField;
	}
}