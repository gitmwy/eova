package com.eova.template.singlechart;

import com.eova.template.common.config.TemplateConfig;
import com.eova.template.single.SingleTemplate;

public class SingleChartTemplate extends SingleTemplate {

	@Override
	public String name() {
		return "单表图";
	}

	@Override
	public String code() {
		return TemplateConfig.SINGLE_CHART;
	}

}
