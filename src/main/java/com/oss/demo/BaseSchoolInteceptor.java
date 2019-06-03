package com.oss.demo;

import java.util.Date;
import java.util.UUID;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.utils.xx;
import com.eova.common.utils.time.FormatUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class BaseSchoolInteceptor extends MetaObjectIntercept {

	@Override
	public void queryBefore(AopContext ac) throws Exception {
		super.queryBefore(ac);
	}

	@Override
	public void queryAfter(AopContext ac) throws Exception {
		super.queryAfter(ac);
	}

	@Override
	public void updateInit(AopContext ac) throws Exception {
		super.updateInit(ac);
	}

	@Override
	public String updateBefore(AopContext ac) throws Exception {

		Record r = ac.record;

		// 获取用户信息 更新
		Integer agentid = r.getInt("[agentid]");
		Integer areaid = r.getInt("[areaid]");
		Integer cityid = r.getInt("[cityid]");
		Integer classtotalnum = r.getInt("[classtotalnum]");
		// String createbyuserid = r.get("createbyuserid"); // 创建人不修改
		String dbname = r.getStr("[dbname]");
		// String fid = String.valueOf(r.get("fid")); // 不更新
		String flnkid = r.getStr("flnkid"); // 不更新
		Boolean isdeleted = r.getBoolean("[isdeleted]");
		Integer issys = r.getInt("[issys]");
		String marksch = r.getStr("[marksch]");
		Integer pharseid = r.getInt("[pharseid]");
		Integer pointmode = r.getInt("[pointmode]");
		Integer power = r.getInt("[power]");
		String representflnkid = r.getStr("[representflnkid]");
		String schoolid = r.getStr("[schoolid]");
		String schoolmaster = r.getStr("[schoolmaster]");
		String schoolmasterphone = r.getStr("[schoolmasterphone]");
		String schoolname = r.getStr("[schoolname]");
		String servergateway = r.getStr("[servergateway]");
		String serverurl = r.getStr("[serverurl]");
		Integer studenttotalnum = r.getInt("[studenttotalnum]");
		Integer teachertotalnum = r.getInt("[teachertotalnum]");
		Date date = r.getDate("[createdate]");
		String createdate = null;
		if (date != null) {
			createdate = FormatUtil.format(date, FormatUtil.YYYY_MM_DD_HH_MM_SS);
		}

		Db.update(
				"update BaseSchool set agentid=?,areaid=?,cityid=?,classtotalnum=?,createdate=?,dbname=?,isdeleted=?,"
						+ "issys=?,marksch=?,pharseid=?,pointmode=?,power=?,representflnkid=?,schoolid=?,schoolmaster=?,schoolmasterphone=?,"
						+ "schoolname=?,servergateway=?,serverurl=?,studenttotalnum=?,teachertotalnum=? where flnkid=?",
				agentid, areaid, cityid, classtotalnum, createdate, dbname, isdeleted, issys, marksch, pharseid,
				pointmode, power, representflnkid, schoolid, schoolmaster, schoolmasterphone, schoolname, servergateway,
				serverurl, studenttotalnum, teachertotalnum, flnkid);

		return super.updateBefore(ac);
	}

	@Override
	public String updateAfter(AopContext ac) throws Exception {
		System.out.println("BaseSchoolInteceptor.updateAfter...");
		return super.updateAfter(ac);
	}

	@Override
	public String updateSucceed(AopContext ac) throws Exception {
		System.out.println("BaseSchoolInteceptor.updateSucceed...");
		return super.updateSucceed(ac);
	}

	@Override
	public void addInit(AopContext ac) throws Exception {

		System.out.println("BaseSchoolInteceptor.addInit...");

		super.addInit(ac);
	}

	@Override
	public String addBefore(AopContext ac) throws Exception {

		System.out.println("BaseSchoolInteceptor.addBefore...");

		Record r = ac.record;

		// 获取用户信息 新增
		String flnkid = UUID.randomUUID().toString();
		Integer agentid = r.getInt("[agentid]");
		Integer areaid = r.getInt("[areaid]");
		Integer cityid = r.getInt("[cityid]");
		Integer classtotalnum = r.getInt("[classtotalnum]");
		String createbyuserid = r.getStr("[createbyuserid]");
		String dbname = r.getStr("[dbname]");
		Boolean isdeleted = r.getBoolean("[isdeleted]");
		Integer issys = r.getInt("[issys]");
		String marksch = r.getStr("[marksch]");
		Integer pharseid = r.getInt("[pharseid]");
		Integer pointmode = r.getInt("[pointmode]");
		Integer power = r.getInt("[power]");
		String representflnkid = r.getStr("[representflnkid]");
		String schoolid = r.getStr("[schoolid]");
		String schoolmaster = r.getStr("[schoolmaster]");
		String schoolmasterphone = r.getStr("[schoolmasterphone]");
		String schoolname = r.getStr("[schoolname]");
		String servergateway = r.getStr("[servergateway]");
		String serverurl = r.getStr("[serverurl]");
		Integer studenttotalnum = r.getInt("[studenttotalnum]");
		Integer teachertotalnum = r.getInt("[teachertotalnum]");
		Date date = r.getDate("[createdate]");
		String createdate = null;
		if (date != null) {
			createdate = FormatUtil.format(date, FormatUtil.YYYY_MM_DD);
		}

		// FID 不能输入
		String sql = "insert baseschool(flnkid,schoolid,schoolname,serverurl,servergateway,createbyuserid,createdate,"
				+ "isdeleted,dbname,cityid,pharseid,power,classtotalnum,studenttotalnum,teachertotalnum,representflnkid,"
				+ "schoolmaster,schoolmasterphone,agentid,pointmode,marksch,areaid,issys)"
				+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		System.out.println("sql: " + sql);

		Db.update(sql, flnkid, schoolid, schoolname, serverurl, servergateway, createbyuserid, createdate, isdeleted,
				dbname, cityid, pharseid, power, classtotalnum, studenttotalnum, teachertotalnum, representflnkid,
				schoolmaster, schoolmasterphone, agentid, pointmode, marksch, areaid, issys);

		return super.addBefore(ac);
	}

	@Override
	public String addAfter(AopContext ac) throws Exception {

		return super.addAfter(ac);
	}

	@Override
	public String deleteBefore(AopContext ac) throws Exception {

		Record r = ac.record;

		// 获取用户信息 新增
		String flnkid = r.get("flnkid");
		Boolean isdeleted = r.getBoolean("[isdeleted]");
		Db.update("update baseschool set isdeleted = ? where flnkid = ?", isdeleted, flnkid);

		return super.deleteBefore(ac);
	}

	@Override
	public String deleteAfter(AopContext ac) throws Exception {

		return super.deleteAfter(ac);
	}

	@Override
	public String deleteSucceed(AopContext ac) throws Exception {

		return super.deleteSucceed(ac);
	}

}
