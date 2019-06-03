package com.oss.demo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.utils.time.FormatUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class VTeachersInterceptor extends MetaObjectIntercept {

	@Override
	public String addAfter(AopContext ac) throws Exception {

		Record r = ac.record;

		r.set("[createtime]", FormatUtil.format(new Date(), FormatUtil.YYYY_MM_DD_HH_MM_SS));
		r.set("[creater]", "SYS");

		String id = r.get("id");
		String roleid = r.getStr("[roleid]");
		String role = r.getStr("[role]");
		String username = r.getStr("[username]");
		Integer userlevel = r.getInt("[userlevel]");
		String usernumber = r.getStr("[usernumber]");
		String usersex = r.getStr("[usersex]");
		String useraddress = r.getStr("[useraddress]");
		String detailtablename = r.getStr("[detailtablename]");
		String createtime = FormatUtil.format(new Date(), FormatUtil.YYYY_MM_DD_HH_MM_SS);
		String creater = "SYS";
		// String modifytime = FormatUtil.format(new Date(),
		// FormatUtil.YYYY_MM_DD_HH_MM_SS);
		// String modifier = "SYS";
		Boolean isdelete = false;
		Boolean isenable = r.getBoolean("[isenable]");
		String useraccount = r.getStr("useraccount");
		String password = r.getStr("[password]");
		Date lastlogin = r.getDate("[lastlogintime]");
		String lastlogintime = null;
		if (lastlogin != null) {
			lastlogintime = FormatUtil.format(lastlogin, FormatUtil.YYYY_MM_DD_HH_MM_SS);
		}
		String lastlogintype = r.getStr("[lastlogintype]");
		BigDecimal canuseamount = r.getBigDecimal("[canuseamount]");
		BigDecimal totalamount = r.getBigDecimal("[totalamount]");
		String vipid = r.getStr("[vipid]");
		Date vipend = r.getDate("[vipendtime]");
		String vipendtime = null;
		if (vipend != null) {
			vipendtime = FormatUtil.format(vipend, FormatUtil.YYYY_MM_DD_HH_MM_SS);
		}

		Db.update(
				"insert into m_userbaseinfos ([id],[roleid],[role],[username],[userlevel],"
						+ "[usernumber],[usersex],[useraddress],[detailtablename],[createtime],[creater],"
						+ "[isdelete],[isenable],[useraccount],[password],[lastlogintime],[lastlogintype],"
						+ "[canuseamount],[totalamount],[vipid],[vipendtime]) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
				id, roleid, role, username, userlevel, usernumber, usersex, useraddress, detailtablename, createtime,
				creater, isdelete, isenable, useraccount, password, lastlogintime, lastlogintype, canuseamount,
				totalamount, vipid, vipendtime);

		String teacherid = id;
		Db.update("insert into m_teachers (teacherid,[createtime],[creater],[isdelete],[isenable]) values(?,?,?,?,?)",
				teacherid, createtime, creater, isdelete, isenable);

		return super.addBefore(ac);
	}

	@Override
	public String updateBefore(AopContext ac) throws Exception {

		Record r = ac.record;

		String id = r.get("id");
		String roleid = r.getStr("[roleid]");
		String role = r.getStr("[role]");
		String username = r.getStr("[username]");
		Integer userlevel = r.getInt("[userlevel]");
		String usernumber = r.getStr("[usernumber]");
		String usersex = r.getStr("[usersex]");
		String useraddress = r.getStr("[useraddress]");
		String detailtablename = r.getStr("[detailtablename]");
		// String createtime = FormatUtil.format(new Date(),
		// FormatUtil.YYYY_MM_DD_HH_MM_SS);
		// String creater = "SYS";
		String modifytime = FormatUtil.format(new Date(), FormatUtil.YYYY_MM_DD_HH_MM_SS);
		String modifier = "SYS";
		Boolean isdelete = false;
		Boolean isenable = r.getBoolean("[isenable]");
		String useraccount = r.getStr("useraccount");
		String password = r.getStr("[password]");
		Date lastlogin = r.getDate("[lastlogintime]");
		String lastlogintime = null;
		if (lastlogin != null) {
			lastlogintime = FormatUtil.format(lastlogin, FormatUtil.YYYY_MM_DD_HH_MM_SS);
		}
		String lastlogintype = r.getStr("[lastlogintype]");
		BigDecimal canuseamount = r.getBigDecimal("[canuseamount]");
		BigDecimal totalamount = r.getBigDecimal("[totalamount]");
		String vipid = r.getStr("[vipid]");
		Date vipend = r.getDate("[vipendtime]");
		String vipendtime = null;
		if (vipend != null) {
			vipendtime = FormatUtil.format(vipend, FormatUtil.YYYY_MM_DD_HH_MM_SS);
		}

		Db.update(
				"update m_userbaseinfos set roleid=?,role=?,username=?,userlevel=?,"
						+ "usernumber=?,usersex=?,useraddress=?,detailtablename=?,modifytime=?,modifier=?,"
						+ "isdelete=?,isenable=?,useraccount=?,password=?,lastlogintime=?,lastlogintype=?,"
						+ "canuseamount=?,totalamount=?,vipid=?,vipendtime=? where id=?",
				roleid, role, username, userlevel, usernumber, usersex, useraddress, detailtablename, modifytime,
				modifier, isdelete, isenable, useraccount, password, lastlogintime, lastlogintype, canuseamount,
				totalamount, vipid, vipendtime, id);

		// String modifytime = FormatUtil.format(new Date(),
		// FormatUtil.YYYY_MM_DD_HH_MM_SS);
		// String modifier = "SYS";
		// Boolean isdelete = false;
		// Boolean isenable = r.getBoolean("[isenable]");

		String teacherid = id;
		Db.update("update m_teachers set modifier=?,modifytime=?,isdelete=?,isenable=? where teacherid=?", modifier,
				modifytime, isdelete, isenable, teacherid);

		return super.updateBefore(ac);
	}

}
