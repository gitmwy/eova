package com.oss.demo;

import java.util.UUID;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.jfinal.plugin.activerecord.Record;

public class CDictionaryInterceptor extends MetaObjectIntercept {

	@Override
	public String addBefore(AopContext ac) throws Exception {

		Record r = ac.record;
		String id = UUID.randomUUID().toString();
		r.set("id", id);

		return super.addBefore(ac);
	}

}
