package com.eova.template.single;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eova.model.Button;
import com.eova.template.Template;
import com.eova.template.common.config.TemplateConfig;
import com.eova.template.common.util.TemplateUtil;

public class SingleTemplate implements Template {

	@Override
	public String name() {
		return "单表";
	}

	@Override
	public String code() {
		return TemplateConfig.SINGLE_GRID;
	}

	@Override
	public Map<Integer, List<Button>> getBtnMap() {
		Map<Integer, List<Button>> btnMap = new HashMap<>();

		{
			List<Button> btns = new ArrayList<>();
			
			btns.add(TemplateUtil.getQueryButton());
			btns.add(new Button("新增", "/eova/template/single/btn/add_lay.html", false));
			btns.add(new Button("修改", "/eova/template/single/btn/update_lay.html", false));
			btns.add(new Button("删除", "/eova/template/single/btn/delete_lay.html", false));
			btns.add(new Button("查看", "/eova/template/single/btn/detail_lay.html", false));
			btns.add(new Button("导出", "/eova/template/common/export_lay.html", false));
			btns.add(new Button("导入", "/eova/template/single/btn/import_lay.html", false));
			btns.add(new Button("隐藏", "/eova/template/single/btn/hide_lay.html", true));
			
			
			
			
			btnMap.put(0, btns);
		}

		return btnMap;
	}

}
