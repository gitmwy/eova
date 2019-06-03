package com.eova.template.diy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eova.model.Button;
import com.eova.template.Template;
import com.eova.template.common.config.TemplateConfig;
import com.eova.template.common.util.TemplateUtil;

public class DiyTemplate implements Template {

	@Override
	public String name() {
		return "自定义功能";
	}

	@Override
	public String code() {
		return TemplateConfig.DIY;
	}

	@Override
	public Map<Integer, List<Button>> getBtnMap() {
		Map<Integer, List<Button>> btnMap = new HashMap<>();

		{
			List<Button> btns = new ArrayList<>();
			btns.add(TemplateUtil.getQueryButton());
			btnMap.put(0, btns);
		}

		return btnMap;
	}

}
