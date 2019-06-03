package com.eova.template.masterslave;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eova.model.Button;
import com.eova.template.Template;
import com.eova.template.common.config.TemplateConfig;
import com.eova.template.common.util.TemplateUtil;

public class MasterSlaveTemplate implements Template {

	@Override
	public String name() {
		return "主子表";
	}

	@Override
	public String code() {
		return TemplateConfig.MASTER_SLAVE_GRID;
	}

	@Override
	public Map<Integer, List<Button>> getBtnMap() {
		Map<Integer, List<Button>> btnMap = new HashMap<>();
		
		{
			List<Button> btns = new ArrayList<>();
			btns.add(TemplateUtil.getQueryButton());
			btns.add(new Button("新增", "/eova/template/masterslave/btn1/add_lay.html", false));
			btns.add(new Button("修改", "/eova/template/masterslave/btn1/update_lay.html", false));
			btns.add(new Button("删除", "/eova/template/masterslave/btn1/delete_lay.html", false));
			btns.add(new Button("查看", "/eova/template/masterslave/btn1/detail_lay.html", false));
			btnMap.put(0, btns);
		}
		{
			List<Button> btns = new ArrayList<>();
			btns.add(new Button("{0}新增", "/eova/template/masterslave/btn2/add_lay.html", false));
			btns.add(new Button("{0}修改", "/eova/template/masterslave/btn2/update_lay.html", false));
			btns.add(new Button("{0}删除", "/eova/template/masterslave/btn2/delete_lay.html", false));
			btns.add(new Button("{0}查看", "/eova/template/masterslave/btn2/detail_lay.html", false));
			btnMap.put(1, btns);
		}
		
		return btnMap;
	}

}
