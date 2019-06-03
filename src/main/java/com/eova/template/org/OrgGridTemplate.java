package com.eova.template.org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eova.model.Button;
import com.eova.template.Template;
import com.eova.template.common.config.TemplateConfig;
import com.eova.template.common.util.TemplateUtil;

public class OrgGridTemplate implements Template {

	@Override
	public String name() {
		return "组织";
	}

	@Override
	public String code() {
		return TemplateConfig.ORG_GRID;
	}

	@Override
	public Map<Integer, List<Button>> getBtnMap() {
		Map<Integer, List<Button>> btnMap = new HashMap<>();

		{
			List<Button> btns = new ArrayList<>();
			
			//目前和单表的功能一致（测试看看是否需要调整）
			btns.add(TemplateUtil.getQueryButton());
			btns.add(new Button("新增", "/eova/template/org/btn/add_lay.html", false));
			btns.add(new Button("修改", "/eova/template/org/btn/update_lay.html", false));
			btns.add(new Button("删除", "/eova/template/org/btn/delete_lay.html", false));
			btns.add(new Button("查看", "/eova/template/org/btn/detail_lay.html", false));
			btns.add(new Button("导入", "/eova/template/org/btn/import_lay.html", false));
			btns.add(new Button("隐藏", "/eova/template/org/btn/hide_lay.html", true));

			btnMap.put(0, btns);
		}

		return btnMap;
	}

}
