/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.widget.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.eova.common.utils.xx;
import com.eova.core.menu.config.TreeConfig;
import com.eova.widget.WidgetUtil;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Record;


/**
 * Tree 工具类
 * 
 * @author Jieven
 * 
 */
public class TreeUtil {
	
	/**
	 * 转化为Tree所需JSON结构
	 * 
	 * @param reList
	 * @return
	 */
	public static String toTreeJson(List<Record> list, TreeConfig treeConfig) {
		Map<String, Record> map = WidgetUtil.listToLinkedMap(list, treeConfig);
		return map2TreeJson(map, treeConfig);
	}
	
	public static String map2TreeJson(Map<String, Record> temp, TreeConfig treeConfig) {
		
		String rootPid = treeConfig.getRootPid();
		String iconField = treeConfig.getIconField();
		String parentField = treeConfig.getParentField();
		
		// EasyUI 公共数据格式处理
		for (Map.Entry<String, Record> map : temp.entrySet()) {
			Record x = map.getValue();
			// 是否默认折叠
			String state = "open";
			Boolean isOpen = x.getBoolean("open");
			if (!xx.isEmpty(isOpen) && !isOpen) {
				state = "closed";
			}
			x.set("state", state);
			x.set("iconCls", x.getStr(iconField));
			// 移除冗余属性
			x.remove("open");
			x.remove(iconField);
		}

		// 创建根节点
		temp.put(rootPid, new Record());

		// 将temp整理成Tree结构
		for (Map.Entry<String, Record> map : temp.entrySet()) {

			// 跳过新增的树根
			if (map.getKey() == rootPid) {
				continue;
			}

			Record re = map.getValue();
			String pid = re.get(parentField).toString();
			Record parent = temp.get(pid);
			if (parent == null) {
				// 父节点为空说明已经遍历到最外层父节点，直接跳过
				continue;
			}
			// System.out.println(re.getStr("name"));
			// 获取父节点子集
			List<Record> children = parent.get("children");
			if (children == null) {
				children = new ArrayList<Record>();
				temp.get(pid).set("children", children);
			}
			// 将当前节点加入父节点子集
			children.add(re);
		}

		// 组装Tree Json
		StringBuilder sb = new StringBuilder("[");
		// 获取根节点
		List<Record> childList = temp.get(rootPid).get("children");
		for (Record x : childList) {
			if (!xx.isEmpty(x.get("children"))) {
				// 父节点默认折叠
				//x.set("state", "closed");
			}
			sb.append(JsonKit.toJson(x));
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]");
		//System.out.println(sb.toString());
		
		// 大小写与EasyUI兼容问题：
		String json = sb.toString();
		json = json.replaceAll("iconcls", "iconCls");

		return json;
	}

}