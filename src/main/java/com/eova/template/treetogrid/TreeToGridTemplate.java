package com.eova.template.treetogrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eova.model.Button;
import com.eova.template.common.config.TemplateConfig;
import com.eova.template.common.util.TemplateUtil;
import com.eova.template.single.SingleTemplate;

public class TreeToGridTemplate extends SingleTemplate {

	@Override
	public String name() {
		return "树&表";
	}

	@Override
	public String code() {
		return TemplateConfig.SINGLE_TREE;
	}

	@Override
	public Map<Integer, List<Button>> getBtnMap() {
		Map<Integer, List<Button>> btnMap = new HashMap<>();

		{
			List<Button> btns = new ArrayList<>();
			btns.add(TemplateUtil.getQueryButton());
			btns.add(new Button("新增", "/eova/template/treetogrid/btn/add_lay.html", false));
			btns.add(new Button("修改", "/eova/template/treetogrid/btn/update_lay.html", false));
			btns.add(new Button("删除", "/eova/template/treetogrid/btn/delete_lay.html", false));
			btns.add(new Button("查看", "/eova/template/treetogrid/btn/detail_lay.html", false));
			btnMap.put(0, btns);
		}

		return btnMap;
	}

}
