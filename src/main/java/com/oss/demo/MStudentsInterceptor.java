package com.oss.demo;

import java.util.Date;
import java.util.UUID;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.utils.time.FormatUtil;
import com.jfinal.plugin.activerecord.Record;

public class MStudentsInterceptor extends MetaObjectIntercept {

	@Override
	public String addBefore(AopContext ac) throws Exception {

		Record r = ac.record;

		r.set("[createtime]", FormatUtil.format(new Date(), FormatUtil.YYYY_MM_DD_HH_MM_SS));
		r.set("[creater]", "SYS");

		return super.addBefore(ac);
	}

	@Override
	public String updateBefore(AopContext ac) throws Exception {

		Record r = ac.record;

		r.remove("[createtime]");

		r.set("[modifytime]", FormatUtil.format(new Date(), FormatUtil.YYYY_MM_DD_HH_MM_SS));
		r.set("[modifier]", "SYS");

		return super.updateBefore(ac);
	}

}
