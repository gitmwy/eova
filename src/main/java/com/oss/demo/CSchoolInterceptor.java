package com.oss.demo;

import java.util.Date;
import java.util.UUID;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.utils.time.FormatUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class CSchoolInterceptor extends MetaObjectIntercept {


	@Override
	public String addBefore(AopContext ac) throws Exception {

		Record r = ac.record;
		String id = UUID.randomUUID().toString();
		
		r.set("id", id);
		r.set("[createtime]", FormatUtil.format(new Date(), FormatUtil.YYYY_MM_DD_HH_MM_SS));
		r.set("[creater]", "SYS");

		return super.addBefore(ac);
	}


	@Override
	public String updateBefore(AopContext ac) throws Exception {

		Record r = ac.record;
//		Date createtime = r.getDate("[createtime]");
//		if(createtime!=null){
//			r.set("[createtime]", FormatUtil.format(createtime, FormatUtil.YYYY_MM_DD_HH_MM_SS));
//		}else{
//			r.set("[createtime]", FormatUtil.format(new Date(), FormatUtil.YYYY_MM_DD_HH_MM_SS));	
//		}
		
		
		
		r.set("[modifytime]", FormatUtil.format(new Date(), FormatUtil.YYYY_MM_DD_HH_MM_SS));
		r.set("[modifier]", "SYS");
		
		return super.updateBefore(ac);
	}

	@Override
	public String deleteBefore(AopContext ac) throws Exception {

		System.out.println("CSchoolInterceptor.deleteBefore...");
//
//		Record r = ac.record;
//
//		// 获取用户信息 新增
//		String id = r.getStr("id");
//		Boolean isdelete = r.getBoolean("[isdelete]");
//		Db.update("update c_dictionary set isdelete = ? where id = ?", isdelete, id);

		return "";
	}

	@Override
	public String deleteAfter(AopContext ac) throws Exception {
		// TODO Auto-generated method stub
		return super.deleteAfter(ac);
	}

}
