package com.oss.demo;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;

public class BaseDictionaryInterceptor extends MetaObjectIntercept {

	@Override
	public void addInit(AopContext ac) throws Exception {

		super.addInit(ac);
	}

	@Override
	public String addBefore(AopContext ac) throws Exception {

		return super.addBefore(ac);
	}

	@Override
	public String addAfter(AopContext ac) throws Exception {

		return super.addAfter(ac);
	}

	@Override
	public void updateInit(AopContext ac) throws Exception {
		super.updateInit(ac);
	}

	@Override
	public String updateBefore(AopContext ac) throws Exception {

		return super.updateBefore(ac);
	}

	@Override
	public String updateAfter(AopContext ac) throws Exception {
		return super.updateAfter(ac);
	}

	@Override
	public String deleteBefore(AopContext ac) throws Exception {

		return super.deleteBefore(ac);
	}

	@Override
	public String deleteAfter(AopContext ac) throws Exception {
		return super.deleteAfter(ac);
	}

}
