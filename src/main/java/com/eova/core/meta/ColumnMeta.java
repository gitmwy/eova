package com.eova.core.meta;

import com.alibaba.fastjson.JSONObject;
import com.eova.common.utils.xx;
import com.eova.config.EovaConfig;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class ColumnMeta {
	
	public String ds;				// 数据源名
	public String table;			// 数据表名
	
	public int position;			// 序号
	public String name;				// 字段名
	
	public int dataType;			// 数据类型
	public String dataTypeName;		// 数据类型名称
	public int dataSize;			// 整数位长度
	public int dataDecimal;			// 小数位长度
	
	public boolean isNull;			// 是否允许空值
	public boolean isAuto;			// 是否自增
	
	public String defaultValue;		// 默认值
	public String remarks;			// 注释
	
	public ColumnMeta(String ds, String table, JSONObject o){
		this.ds = ds;
		this.table = table;
		
		this.position = o.getIntValue("ORDINAL_POSITION");
		this.name = o.getString("COLUMN_NAME");
		
		this.dataType = o.getIntValue("DATA_TYPE");
		this.dataTypeName = o.getString("TYPE_NAME");
		this.dataSize = o.getIntValue("COLUMN_SIZE");
		this.dataDecimal = o.getIntValue("DECIMAL_DIGITS");
		
		this.isNull = "YES".equalsIgnoreCase(o.getString("IS_NULLABLE")) ? true : false;
		this.isAuto = "YES".equalsIgnoreCase(o.getString("IS_AUTOINCREMENT")) ? true : false;
		
		this.defaultValue = o.getString("COLUMN_DEF");
		this.remarks = o.getString("REMARKS");

		// 月读：自动处理注释 
		buildRemarks();
	}
	
	
	/**
	 * 自动根据注释解析列名和字典
	 * 
	 * @param remarks 注释
	 * @param table 表
	 * @param en 字段
	 * @return
	 */
	public void buildRemarks() {
		if (xx.isOneEmpty(this.ds, this.table, this.remarks)) {
			return;
		}
		// eg. 状态:/：1=/-上架,/，2=/-售罄,3=下架 ,4=过期
		// 
		String[] ss = null;
		try {
			// 获取第1项注释作为列名
			if (remarks.contains(":") || remarks.contains("：")) {
				ss = remarks.split(":|：");
			}
			if (xx.isEmpty(ss)) {
				return;
			}
			this.remarks = ss[0];

			// 获取第2项注释作为字典
			if (ss.length > 1) {
				String ss1 = ss[1];
				// 如果没有=号 说明是其它描述eg. 原价:大于等于0
				if (ss1.contains("=") || ss1.contains("-")) {
					
					LogKit.info(String.format("自动生成字典[%s - %s]", this.table, this.name));
					
					String[] dicts = ss1.split(",|，");
					
					String dictTable = EovaConfig.getProps().get("main_dict_table");
					// 清除字典
					Db.use(this.ds).update(String.format("delete from %s where object = ? and field = ?", dictTable), this.table, this.name);
					// 生成字典
					for (String dict : dicts) {
						
						String[] sss = dict.split("=|-|——");
						String value = sss[0];
						String key = sss[1];
						
						// 保存字典
						Record r = new Record();
						r.set("value", value);
						r.set("name", key);
						r.set("object", this.table);
						r.set("field", this.name);
						r.set("ext", "");
						Db.use(this.ds).save(dictTable, r);
					}
				}
			}
		} catch (Exception e) {
			LogKit.error("元字段备注预处理异常:" + remarks, e);
		}
	}
}