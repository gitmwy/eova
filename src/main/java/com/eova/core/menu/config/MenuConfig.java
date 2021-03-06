/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.core.menu.config;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 菜单配置
 * 
 * @author Jieven
 * 
 */
public class MenuConfig {
	// 主对象Code
	private String objectCode;
	// 主对象外键字段
	private String objectField;

	// 子对象Code列表
	private List<String> objects;
	// 子对象关联字段列表
	private List<String> fields;

	// 树属性
	private TreeConfig tree;
	
	// 图属性
	private ChartConfig chart;

	public MenuConfig() {
	}

	public MenuConfig(String str) {
		JSONObject json = JSON.parseObject(str);
		this.objectCode = json.getString("objectCode");
		this.objectField = json.getString("objectField");
		this.objects = JSON.parseArray(json.getString("objects"), String.class);
		this.fields = JSON.parseArray(json.getString("fields"), String.class);
		this.tree = JSON.parseObject(json.getString("tree"), TreeConfig.class);
		this.chart = JSON.parseObject(json.getString("chart"), ChartConfig.class);
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

	public List<String> getObjects() {
		return objects;
	}

	public void setObjects(List<String> objects) {
		this.objects = objects;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public TreeConfig getTree() {
		return tree;
	}

	public void setTree(TreeConfig tree) {
		this.tree = tree;
	}

	public ChartConfig getChart() {
		return chart;
	}

	public void setChart(ChartConfig chart) {
		this.chart = chart;
	}

	public static void main(String[] args) {
		MenuConfig mc = new MenuConfig();
		TreeConfig tc = new TreeConfig();
		tc.setObjectCode("eova_menu_code");
		tc.setObjectField("code");
		
		tc.setIdField("id");
		tc.setParentField("parent_id");
		tc.setTreeField("name");
		tc.setIconField("iconskip");
		mc.setTree(tc);

		mc.setObjectCode("eova_button_code");
		mc.setObjectField("menu_code");

		String s = JSONObject.toJSONString(mc);
		System.out.println(s);
	}

}
