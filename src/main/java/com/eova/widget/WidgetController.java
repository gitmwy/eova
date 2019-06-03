/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.widget;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eova.common.base.BaseCache;
import com.eova.common.utils.xx;
import com.eova.common.utils.db.DbUtil;
import com.eova.config.EovaConfig;
import com.eova.config.EovaConst;
import com.eova.config.PageConst;
import com.eova.engine.DynamicParse;
import com.eova.engine.EovaExp;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * EOVA 控件
 *
 * @author Jieven
 *
 */
public class WidgetController extends Controller {

	/**
	 * 查找Dialog
	 */
	public void toFind() {
		String template = this.getPara(PageConst.Template);//模板
		if (!xx.isEmpty(template)) 
			render("/eova/widget/find/find_"+template+".html");
		else
			render("/eova/widget/find/find.html");
		
	}

	/**
	 * 查找框Dialog
	 */
	public void find() {

		List<Object> parmList = new ArrayList<Object>();

		String url = "/widget/findJson?";

		String exp = getPara("exp");
		String code = getPara("code");
		String field = getPara("field");
		boolean isMultiple = getParaToBoolean("multiple", false);
		// 自定义表达式
		if (xx.isEmpty(exp)) {
			// 根据表达式获取ei
			MetaField ei = MetaField.dao.getByObjectCodeAndEn(code, field);
			exp = ei.getStr("exp");
			url += "code=" + code + "&field=" + field;
		} else {
			url += "exp=" + exp;
			// 预处理表达式
			try {
				String[] strs = exp.split(",");
				if (strs.length > 1) {
					exp = EovaConfig.getExps().get(strs[0]);
					for (int i = 1; i < strs.length; i++) {
						parmList.add(getSqlParam(strs[i]));
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("预处理自定义查找框表达式异常，Exp=" + exp);
			}
		}

		// 动态解析变量和逻辑运算
		exp = DynamicParse.buildSql(exp, this.getSessionAttr(EovaConst.USER));

		// 根据表达式构建元数据
		EovaExp se = new EovaExp(exp);
		MetaObject mo = se.getObject();
		List<MetaField> mfs = se.getFields();
		if (isMultiple) {
			mo.set("is_single", false);
		}
		// mo.set("is_celledit", true);
		// for (MetaField mf : mfs) {
		// mf.set("is_edit", true);
		// mf.put("editor", "eovatext");
		// }

		setAttr("action", url);
		// 用于Grid呈现
		setAttr("objectJson", JsonKit.toJson(mo));
		setAttr("fieldsJson", JsonKit.toJson(mfs));
		// 用于query条件
		setAttr("itemList", mfs);
		setAttr("pk", se.pk);

		toFind();
	}

	/**
	 * Find Dialog Grid Get JSON
	 */
	public void findJson() {

		List<Object> parmList = new ArrayList<Object>();

		String exp = getPara("exp");
		String code = getPara("code");
		String en = getPara("field");
		if (xx.isEmpty(exp)) {
			// 根据表达式获取ei
			MetaField ei = MetaField.dao.getByObjectCodeAndEn(code, en);
			exp = ei.getStr("exp");
		} else {
			// 预处理表达式
			try {
				String[] strs = exp.split(",");
				if (strs.length > 1) {
					exp = EovaConfig.getExps().get(strs[0]);
					for (int i = 1; i < strs.length; i++) {
						parmList.add(getSqlParam(strs[i]));
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("预处理自定义查找框表达式异常，Exp=" + exp);
			}
		}

		// 动态解析变量和逻辑运算
		exp = DynamicParse.buildSql(exp, this.getSessionAttr(EovaConst.USER));
		// 解析表达式
		EovaExp se = new EovaExp(exp);
		String select = se.simpleSelect;
		String from = se.from;
		String where = se.where;
		String ds = se.ds;
		List<MetaField> eis = se.getFields();

		// 获取分页参数
		int pageNumber = getParaToInt(PageConst.PAGENUM, 1);
		int pageSize = getParaToInt(PageConst.PAGESIZE, 15);

		// 获取条件
		where = WidgetManager.getWhere(this, eis, parmList, where);
		Object[] parm = new Object[parmList.size()];
		parmList.toArray(parm);

		// 获取排序
		String sort = WidgetManager.getSort(this, se.order);

		// 分页查询Grid数据
		String sql = from + where + sort;
		Page<Record> page = Db.use(ds).paginate(pageNumber, pageSize, select, DbUtil.formatSql(sql), parm);

		// 将分页数据转换成JSON
		String json = JsonKit.toJson(page.getList());
		json = "{\"status\": 200, \"total\":" + page.getTotalRow() + ",\"rows\":" + json + "}";
		// System.out.println(json);
		
		renderJson(json);
	}

	/**
	 * Find get CN by value
	 */
	public void findCnByEn() {
		String code = getPara(0);
		String en = getPara(1);
		String value = getPara(2);
		
		//System.out.println("===="+this.getPara());
		
		//TableUser-schoolflnkid-171C73D1-4E1C-4780-BA2E-7D324D2ED9A3
		//171C73D1-4E1C-4780-BA2E-7D324D2ED9A3
//		if(this.getPara()!= null && this.getPara().split("-").length == 7){//可能是uuid数据
//			value = this.getPara().substring(this.getPara().length() - 36, this.getPara().length());
//		}
		
		
		// 根据表达式获取元字段
		MetaField ei = MetaField.dao.getByObjectCodeAndEn(code, en);

		String exp = ei.getStr("exp");
		// 动态解析变量和逻辑运算
		exp = DynamicParse.buildSql(exp, this.getSessionAttr(EovaConst.USER));

		// 解析表达式
		EovaExp se = new EovaExp(exp);
		String ds = se.ds;

		// 查询本次所有翻译值
		StringBuilder sb = new StringBuilder();
		if (!xx.isEmpty(value)) {
			sb.append(se.pk);
			sb.append(" in(");
			// 根据当前页数据value列查询外表name列
			for (String id : value.split(",")) {
				// TODO There might be a sb injection risk warning
				sb.append(xx.format(id)).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
		}
		//System.out.println(sb.toString());

		// 根据表达式查询获得翻译的值
		String sql = WidgetManager.addWhere(se, sb.toString());
		System.out.println("sql: "+sql);
		List<Record> txts = Db.use(ds).find(sql);
		// 没有翻译值，直接返回原值
		if (xx.isEmpty(txts)) {
			JSONObject json = new JSONObject();
			json.put("code", 0);
			json.put("data", value);
			renderJson(json.toJSONString());
			return;
		}

		JSONObject json = new JSONObject();
		json.put("code", 1);
		json.put("text_field", se.cn);// 文本字段名
		json.put("data", JsonKit.toJson(txts));// 翻译字典数据
		renderJson(json.toJSONString());
	}

	/**
	 * 获取SQL参数，优先Integer，不能转就当String
	 *
	 * @param str
	 * @return
	 */
	private static Object getSqlParam(String str) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return str;
		}
	}

	/**
	 * Combo Load Data Get JSON
	 */
	public void comboJson() {
		String exp = getPara("exp");

		List<Object> parmList = new ArrayList<Object>();

		MetaField ei = null;
		if (xx.isEmpty(exp)) {
			// 根据元数据获取表达式
			String objectCode = getPara(0);
			String en = getPara(1);
			ei = MetaField.dao.getByObjectCodeAndEn(objectCode, en);
			exp = ei.getStr("exp");
		} else {
			// 预处理自定义表达式
			try {
				String[] strs = exp.split(",");
				if (strs.length > 1) {
					exp = EovaConfig.getExps().get(strs[0]);
					for (int i = 1; i < strs.length; i++) {
						parmList.add(getSqlParam(strs[i]));
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("预处理自定义查找框表达式异常，Exp=" + exp);
			}
		}

		boolean isMultiple = false;
		if (ei != null && ei.getBoolean("is_multiple")) {
			isMultiple = true;
		}

		// 根据表达式构建数据
		String json = buildComboData(exp, parmList, isMultiple);

		// json = "[value,name]";
		// System.out.println(json);
		renderJson(json);
	}

	/**
	 * 根据表达式构建下拉框所需JSON数据
	 *
	 * @param exp 表达式
	 * @param parmList sql参数
	 * @param isMultiple 是否多选
	 * @return 下拉JSON
	 */
	private String buildComboData(String exp, List<Object> parmList, boolean isMultiple) {
		exp = exp.trim();

		// 动态解析变量和逻辑运算
		exp = DynamicParse.buildSql(exp, this.getSessionAttr(EovaConst.USER));

		// 解析表达式
		EovaExp se = new EovaExp(exp);
		String sql = se.sql;

		Object[] paras = new Object[parmList.size()];
		parmList.toArray(paras);
		List<Record> list = Db.use(se.ds).findByCache(BaseCache.SER, sql, sql, paras);
		// 初始化首项
		//initItemToList(isMultiple, list);

		return JsonKit.toJson(list);
	}

	/**
	 * 为下拉列表添加初始项(暂时不用了，前端补齐第一项 作为提示)
	 *
	 * @param isMultiple
	 * @param list
	 */
	private void initItemToList(boolean isMultiple, List<Record> list) {
		// 只有单选需要添加()
		if (!isMultiple) {
			Record re = new Record();
			re.set("id", "");
			re.set("cn", "");
			list.add(0, re);
		}
	}

	public static void main(String[] args) {
		String exp = "selectAreaByLv1AndPid,1,3,abc";
		try {
			String[] strs = exp.split(",");
			exp = EovaConfig.getExps().get(strs[0]);
			for (int i = 1; i < strs.length; i++) {
				System.out.println(getSqlParam(strs[i]) + " " + getSqlParam(strs[i]).getClass());
			}
		} catch (Exception e) {
			throw new RuntimeException("预处理自定义查找框表达式异常，Exp=" + exp);
		}
	}
}