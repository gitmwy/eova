/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.core.button;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import com.eova.common.utils.xx;
import com.eova.common.utils.data.CloneUtil;
import com.eova.config.EovaConst;
import com.eova.core.menu.config.MenuConfig;
import com.eova.model.Button;
import com.eova.model.Menu;
import com.eova.model.MetaObject;
import com.eova.model.RoleBtn;
import com.eova.template.Template;
import com.eova.template.common.config.TemplateConfig;
import com.eova.template.diy.DiyTemplate;
import com.eova.template.masterslave.MasterSlaveTemplate;
import com.eova.template.org.OrgGridTemplate;
import com.eova.template.single.SingleTemplate;
import com.eova.template.singlechart.SingleChartTemplate;
import com.eova.template.singleimg.SingleImgTemplate;
import com.eova.template.singletree.SingleTreeTemplate;
import com.eova.template.treetogrid.TreeToGridTemplate;

/**
 * 按钮构建工厂
 * 
 * @author Jieven
 * @date 2016-11-19
 */
public class ButtonFactory {

	private Template template;

	public ButtonFactory(String templateType) {
		if (templateType.equals(TemplateConfig.DIY)) {
			template = new DiyTemplate();
		} else if (templateType.equals(TemplateConfig.SINGLE_GRID)) {
			template = new SingleTemplate();
		} else if (templateType.equals(TemplateConfig.SINGLE_TREE)) {
			template = new SingleTreeTemplate();
		} else if (templateType.equals(TemplateConfig.SINGLE_CHART)) {
			template = new SingleChartTemplate();
		} else if (templateType.equals(TemplateConfig.MASTER_SLAVE_GRID)) {
			template = new MasterSlaveTemplate();
		} else if (templateType.equals(TemplateConfig.TREE_GRID)) {
			template = new TreeToGridTemplate();
		} else if (templateType.equals(TemplateConfig.IMG_GRID)) {
			template = new SingleImgTemplate();
		}else if (templateType.equals(TemplateConfig.ORG_GRID)) {
			template = new OrgGridTemplate();
		}
	}

	/**
	 * 创建菜单按钮
	 * 
	 * @param menuCode
	 * @param config
	 * @throws Exception
	 */
	public void build(String menuCode, MenuConfig config) {

		Map<Integer, List<Button>> btnMap = template.getBtnMap();

		// 默认按钮组
		List<Button> btns = btnMap.get(0);
		for (int i = 0; i < btns.size(); i++) {
			Button btn = btns.get(i);
			saveButton(btn, menuCode, 0, i);
		}

		// 第1按钮组
		btns = btnMap.get(1);

		// 主子模版：初始化子表功能按钮
		if (template.code().equals(TemplateConfig.MASTER_SLAVE_GRID)) {
			int groupNum = 1;
			for (String code : config.getObjects()) {
				MetaObject object = MetaObject.dao.getByCode(code);
				for (int i = 0; i < btns.size(); i++) {
					// 克隆对象使用
					Button btn = CloneUtil.clone(btns.get(i));
					// 子按钮根据对象命名
					btn.set("name", MessageFormat.format(btn.getStr("name"), object.getName()));
					saveButton(btn, menuCode, groupNum, i);
				}
				groupNum++;
			}
		}
	}

	/**
	 * 保存按钮
	 * 
	 * @param btn 按钮
	 * @param menuCode 菜单编码
	 * @param groupNum 按钮组序号
	 * @param orderNum 按钮序号
	 */
	private void saveButton(Button btn, String menuCode, int groupNum, int orderNum) {
		btn.set("menu_code", menuCode);
		btn.set("is_base", true);
		btn.set("group_num", groupNum);
		btn.set("order_num", orderNum);
		buildButtonUri(btn);// 生成按钮的服务端URI
		btn.save();

		// 自动授权给超管
		autoToAdmin(btn.getInt("id"));
	}

	/**
	 * 自动授权给超级管理员
	 * 
	 * @param btn
	 */
	private void autoToAdmin(int bid) {
		RoleBtn rf = new RoleBtn();
		rf.set("rid", EovaConst.ADMIN_RID);
		rf.set("bid", bid);
		rf.save();
	}

	/**
	 * 构建按钮的资源URI
	 * 
	 * @param button
	 */
	private void buildButtonUri(Button button) {
		StringBuilder sb = new StringBuilder();

		// int id = button.getInt("id");
		int group = button.getInt("group_num");
		String ui = button.getStr("ui");
		String menuCode = button.getStr("menu_code");
		Menu menu = Menu.dao.findByCode(menuCode);
		String type = menu.getStr("type");
		List<String> objects = menu.getConfig().getObjects();
		// 主对象
		String objectCode = menu.getConfig().getObjectCode();
		// 主子模版 对象编码
		if (group > 0) {
			objectCode = objects.get(group - 1);
		}
		
		///eova/template/masterslave/btn1/add_lay.html
				///eova/template/masterslave/btn1/add.html
		String newui = ui.replaceAll("add_.+?.html", "add.html");
				newui = newui.replaceAll("update_.+?.html", "update.html");
				newui = newui.replaceAll("detail_.+?.html", "detail.html");
				
				newui = newui.replaceAll("delete_.+?.html", "delete.html");
				newui = newui.replaceAll("import_.+?.html", "import.html");
				newui = newui.replaceAll("hide_.+?.html", "hide.html");

		// 查询
		if (ui.equals("query")) {
			sb.append(String.format("/%s/list/%s", type, menuCode)).append(";");
			// 主Grid
			sb.append(String.format("/grid/query/%s*", objectCode)).append(";");
			sb.append(String.format("/grid/export/%s*", objectCode)).append(";");
			// 子Grid
			if (TemplateConfig.MASTER_SLAVE_GRID.equals(type)) {
				for (String o : objects) {
					sb.append(String.format("/grid/query/%s*", o)).append(";");
					sb.append(String.format("/grid/export/%s*", o)).append(";");
				}
			}
			// 单表树
			if (TemplateConfig.SINGLE_TREE.equals(type)) {
				sb.append(String.format("/treegrid/query/%s*", "")).append(";");
			}
			// 树&表
			if (TemplateConfig.TREE_GRID.equals(type)) {
				String objectCodeTree = menu.getConfig().getTree().getObjectCode();
				sb.append(String.format("/tree/query/%s*", objectCodeTree)).append(";");
			}
		} else if (ui.endsWith("add.html")
				||newui.endsWith("add.html")) {
			// 新增
			sb.append(String.format("/form/add/%s*", objectCode)).append(";");
			sb.append(String.format("/form/doAdd/%s", objectCode)).append(";");
		} else if (ui.endsWith("update.html")
				||newui.endsWith("update.html")) {
			// 修改
			sb.append(String.format("/form/update/%s*", objectCode)).append(";");
			sb.append(String.format("/form/doUpdate/%s", objectCode)).append(";");
		} else if (ui.endsWith("detail.html")
				||newui.endsWith("detail.html")) {
			// 查看
			sb.append(String.format("/form/detail/%s*", objectCode)).append(";");
		} else if (ui.endsWith("delete.html")
				||newui.endsWith("delete.html")) {
			// 删除
			sb.append(String.format("/grid/delete/%s", objectCode)).append(";");
		} else if (ui.endsWith("hide.html")
				||newui.endsWith("hide.html")) {
			// 隐藏
			sb.append(String.format("/grid/hide/%s", objectCode)).append(";");
		} else if (ui.endsWith("import.html")
				||newui.endsWith("import.html")) {
			// 导入
			sb.append(String.format("/single_grid/importXls/%s", menuCode)).append(";");
			sb.append(String.format("/single_grid/doImportXls/%s", menuCode)).append(";");
		}
		String uri = sb.toString();
		if (!xx.isEmpty(uri)) {
			uri = xx.delEnd(uri, ";");
			System.out.println(uri);
			button.set("bs", uri);
		}
	}

}