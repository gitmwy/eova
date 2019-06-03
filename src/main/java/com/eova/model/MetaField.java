/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.model;

import java.util.List;

import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.fastjson.JSON;
import com.eova.common.base.BaseModel;
import com.eova.common.utils.xx;
import com.eova.common.utils.time.TimestampUtil;
import com.eova.config.EovaConfig;
import com.eova.core.meta.ColumnMeta;
import com.eova.core.meta.MetaEngine;
import com.jfinal.plugin.activerecord.Db;

/**
 * 字段属性
 * 
 * @author Jieven
 * @date 2014-9-10
 */
public class MetaField extends BaseModel<MetaField> {

	private static final long serialVersionUID = -7381270435240459528L;

	public static final MetaField dao = new MetaField();

	public static final String TYPE_TEXT = "文本框";
	public static final String TYPE_NUM = "数字框";
	public static final String TYPE_COMBO = "下拉框";
	public static final String TYPE_FIND = "查找框";
	public static final String TYPE_TIME = "时间框";
	public static final String TYPE_DATE = "日期框";
	public static final String TYPE_TEXTS = "文本域";
	public static final String TYPE_EDIT = "编辑框";
	public static final String TYPE_BOOL = "布尔框";
	public static final String TYPE_AUTO = "自增框";
	public static final String TYPE_IMG = "图片框";
	public static final String TYPE_FILE = "文件框";

	/** 字段数据类型-字符 **/
	public static final String DATATYPE_STRING = "string";
	/** 字段数据类型-数值 **/
	public static final String DATATYPE_NUMBER = "number";
	/** 字段数据类型-时间 **/
	public static final String DATATYPE_TIME = "time";

	public MetaField() {

	}

	public MetaField(String objectCode, ColumnMeta col) {

		if (EovaConfig.isLowerCase) {
			col.name = col.name.toLowerCase();
		}
		
		
		
		this.set("object_code", objectCode);
		
		
			
		
		this.set("en", col.name);
		this.set("cn", col.remarks);
		this.set("order_num", col.position);
		this.set("is_required", !col.isNull);
		this.set("data_type", col.dataType);
		this.set("data_type_name", col.dataTypeName);
		this.set("data_size", col.dataSize);
		this.set("data_decimal", col.dataDecimal);
		//时间的默认值如：mysql-CURRENT_TIMESTAMP 设置了会错误。。。
		String t = col.dataTypeName.toLowerCase();
		if (!t.contains("time") && !t.contains("date")) {
			this.set("defaulter", col.defaultValue);
		}
		
		this.set("is_auto", col.isAuto);

		this.set("type", getFormType(col));
		
		
		// 是否自增
		if (col.isAuto) {
			// 自增框新增禁用
			this.set("add_status", 50);
			// 自增框编辑隐藏
			this.set("update_status", 20);
		}
		// 将注释作为CN,若为空使用EN
		if (xx.isEmpty(this.getCn())) {
			this.set("cn", this.getEn());
		}
		// 默认值
		if (xx.isEmpty(this.getStr("defaulter"))) {
			this.set("defaulter", "");
		}
		// 智能预处理数据
		MetaEngine.build(this);
		
		//数据库类型
		//this.set("dbtype", JdbcUtils.MYSQL);
	}
	
	
	
	/**
	 * 获取表单类型
	 * 
	 * @param isAuto 是否自增
	 * @param typeName 类型
	 * @param size 长度
	 * @return
	 */
	public static String getFormType(ColumnMeta col) {
		String t = col.dataTypeName.toLowerCase();
		int s = col.dataSize;
		if (t.contains("time") || t.contains("date")) {
			return TYPE_TIME;
		} else if (col.isAuto) {
			return TYPE_AUTO;
		} else if (s > 50) {
			return TYPE_TEXTS;
		} else if (s == 1 && (t.equals("tinyint") || t.equals("char"))) {
			return TYPE_BOOL;
		} else {
			// 默认都是文本框
			return TYPE_TEXT;
		}
	}

	public MetaFieldConfig getConfig() {
		String json = this.getStr("config");
		if (xx.isEmpty(json)) {
			return null;
		}
		return JSON.parseObject(json, MetaFieldConfig.class);
	}

	public void setConfig(MetaFieldConfig config) {
		this.set("config", JSON.toJSONString(config));
	}
	
	
//	public <T> T get(String attr) {
//		if("en".equals(attr)){
//			String enValue = super.get(attr);
//			if(enValue != null && enValue.startsWith("[") && enValue.endsWith("]")){
//				return (T)enValue.substring(1, enValue.length()-1);
//			}else{
//				return (T)enValue;
//			}
//		}else{
//			return (T) super.get(attr);
//		}
//			
//		//return (T) super.get(attr);
//		//return (T)(this.attrs.get(attr));
//	}
	
	/**
	 * 获取字段英文名
	 * 
	 * @return
	 */
	public String getEn() {
		
		
		//return this.getStr("en");
//			//特殊字段"[]"包裹，需去掉
		String en = this.getStr("en");
			if(en != null && en.startsWith("[") && en.endsWith("]")){
				en = en.substring(1, en.length()-1);
			}
			
			this.set("en", en);
			
			return en;
	}

	/**
	 * 获取字段中文名
	 * 
	 * @return
	 */
	public String getCn() {
		return this.getStr("cn");
	}

	/**
	 * 获取控件类型
	 * 
	 * @return
	 */
	public String getType() {
		return this.getStr("type");
	}

	/**
	 * 获取数据类型
	 * 
	 * @return
	 */
	public int getDataType() {
		return this.getInt("data_type");
	}

	public String getDataTypeName() {
		return this.getStr("data_type_name").toUpperCase();
	}

	public int getDataSize() {
		return this.getInt("data_size");
	}

	public int getDataDecimal() {
		return this.getInt("data_decimal");
	}

	/**
	 * 获取元字段数据模版(用于模拟元数据)
	 * 
	 * @return
	 */
	public MetaField getTemplate() {
		MetaField mf = this.queryFisrtByCache("select * from eova_field where id = 1");
		return mf;
	}

	/**
	 * 获取元字段信息
	 * 
	 * @param objectCode 对象Code
	 * @return
	 */
	public List<MetaField> queryByObjectCode(String objectCode) {
		return MetaField.dao.queryByCache("select * from eova_field where object_code = ? order by order_num", objectCode);
	}

	/**
	 * 分组获取元字段信息
	 * 
	 * @param objectCode 对象Code
	 * @return
	 */
	public List<MetaField> queryGroupByObjectCode(String objectCode) {
		List<MetaField> mfs = MetaField.dao.queryByCache("select * from eova_field where object_code = ? order by fieldnum, order_num", objectCode);
		return mfs;
	}

	/**
	 * 获取字段
	 * 
	 * @param objectCode 对象Code
	 * @param en 字段Key
	 * @return
	 */
	public MetaField getByObjectCodeAndEn(String objectCode, String en) {
		MetaField ei = MetaField.dao.queryFisrtByCache("select * from eova_field where object_code = ? and en = ? order by order_num", objectCode, en);
		return ei;
	}

	public void deleteByObjectId(int objectId) {
		String sql = "delete from eova_field where object_code = (select code from eova_object where id = ?)";
		Db.use(xx.DS_EOVA).update(sql, objectId);
	}

	public void deleteByObjectCode(String objectCode) {
		String sql = "delete from eova_field where object_code = ?";
		Db.use(xx.DS_EOVA).update(sql, objectCode);
	}

	/**
	 * 查询视图包含的持久化对象Code
	 * 
	 * @param objectCode 视图对象编码
	 * @return
	 */
	public List<MetaField> queryPoCodeByObjectCode(String objectCode) {
		return MetaField.dao.find("SELECT DISTINCT(po_code) from eova_field where object_code = ?", objectCode);
	}

	public List<MetaField> queryFields(String objectCode) {
		List<MetaField> mfs = MetaField.dao.queryByObjectCode(objectCode);
		// 获取Edit 控件类型
		for (MetaField f : mfs) {
			f.getEn();
			String type = f.getType();
			if (type.equals(MetaField.TYPE_BOOL)) {
				f.put("editor", "eovabool");
			} else if (type.equals(MetaField.TYPE_COMBO)) {
				f.put("editor", "eovacombo");
			} else if (type.equals(MetaField.TYPE_FIND)) {
				f.put("editor", "eovafind");
			} else if (type.equals(MetaField.TYPE_TIME)) {
				f.put("editor", "eovatime");
			} else if (type.equals(MetaField.TYPE_TEXTS)) {
				f.put("editor", "eovainfo");
			} else {
				f.put("editor", "eovatext");
			}
		}
		return mfs;
	}

	public List<MetaField> queryFieldsGroup(String objectCode) {
		List<MetaField> mfs = MetaField.dao.queryGroupByObjectCode(objectCode);
		for (MetaField f : mfs) {
			if(xx.isEmpty(f.getStr("exp"))){
				if (f.getType().equals(MetaField.TYPE_FIND)) {
					throw new RuntimeException(String.format("元对象[%s]中的元字段[%s]是查找框，该字段必须填写Eova表达式！", objectCode, f.getEn()));
				} else if(f.getType().equals(MetaField.TYPE_COMBO)){
					throw new RuntimeException(String.format("元对象[%s]中的元字段[%s]是下拉框，该字段必须填写Eova表达式！", objectCode, f.getEn()));
				}
			}
			String defaulter = f.getStr("defaulter");
			if (!xx.isEmpty(defaulter) && defaulter.equalsIgnoreCase("CURRENT_TIMESTAMP")) {
				f.set("defaulter", TimestampUtil.getNow());
			}
		}
		return mfs;
	}

	/**
	 * 更新原字段归属表
	 * @param objectCode 对象编码
	 * @param en 字段名
	 * @param tableName 字段归属表
	 * @return
	 */
	public int updateTableNameByCode(String objectCode, String en, String tableName){
		return Db.use(xx.DS_EOVA).update("update eova_field set table_name = ? where object_code = ? and en = ?", tableName, objectCode, en);
	}
	
	/**
	 * 交换排序
	 * @param sid 原ID
	 * @param tid 目标ID
	 * @param snum 原序号
	 * @param tnum 目标序号
	 */
	public void swapOrderNum(int sid, int tid, int snum, int tnum){
		Db.use(xx.DS_EOVA).update("update eova_field set order_num = ? where id = ?", tnum, sid);
		Db.use(xx.DS_EOVA).update("update eova_field set order_num = ? where id = ?", snum, tid);
	}
	
}