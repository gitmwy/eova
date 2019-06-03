/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.widget;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.alibaba.druid.util.StringUtils;
import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.utils.xx;
import com.eova.common.utils.time.DateUtil;
import com.eova.config.EovaConfig;
import com.eova.config.EovaConst;
import com.eova.config.PageConst;
import com.eova.core.meta.MetaDataType;
import com.eova.core.object.config.MetaObjectConfig;
import com.eova.core.object.config.TableConfig;
import com.eova.engine.DynamicParse;
import com.eova.engine.EovaExp;
import com.eova.model.Menu;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.template.common.config.TemplateConfig;
import com.eova.template.common.util.TemplateUtil;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * 组件公共业务
 *
 * @author Jieven
 *
 */
public class WidgetManager {

	/**
	 * 构建查询
	 *
	 * @param ctrl
	 * @param object
	 * @param menu
	 * @param intercept
	 * @param parmList
	 * @return
	 * @throws Exception
	 */
	public static String buildQuery(Controller ctrl, MetaObject object, Menu menu, MetaObjectIntercept intercept, List<Object> parmList) throws Exception {
		String sql = "";

		String filter = object.getStr("filter");
		// 菜单初始过滤条件优先级高于对象初始过滤条件
		if (menu != null) {
			String menuFilter = menu.getStr("filter");
			if (!xx.isEmpty(menuFilter))
				filter = menuFilter;
		}
		// if (!xx.isEmpty(filter)) {
		// // 不对超级管理员做数据限制
		// User user = (User) ctrl.getSessionAttr(EovaConst.USER);
		// if (user.getRid() == EovaConst.ADMIN_RID) {
		// filter = null;
		// }
		// }

		// 获取条件
		String where = WidgetManager.getWhere(ctrl, object.getFields(), parmList, filter);

		// 获取排序
		String sort = WidgetManager.getSort(ctrl);

		// 分页查询Grid数据
		String view = object.getView();
		sql = "from " + view;

		// 查询前置任务
		if (intercept != null) {
			AopContext ac = new AopContext(ctrl);
			intercept.queryBefore(ac);

			// 追加条件
			if (!xx.isEmpty(ac.condition)) {
				where += ac.condition;
				parmList.addAll(ac.params);
			}
			// 覆盖条件
			if (!xx.isEmpty(ac.where)) {
				where = ac.where;
				parmList.clear();
				parmList.addAll(ac.params);
			}
			// 覆盖排序
			if (!xx.isEmpty(ac.sort)) {
				sort = ac.sort;
			}
		}

		sql += String.format(" %s %s ", where, sort);

		return sql;
	}

	/**
	 * 获取排序
	 *
	 * @param c
	 * @param eo 元对象
	 * @param order 指定排序
	 * @return
	 */
	public static String getSort(Controller c, String order) {

		// 动态解析变量和逻辑运算
		order = DynamicParse.buildSql(order, c.getSessionAttr(EovaConst.USER));

		String sql = "";

		// 指定默认排序方式
		if (!xx.isEmpty(order)) {
			sql = order;
		}

		// 当前Request的排序方式
		String orderField = c.getPara(PageConst.SORT, "");// 获取排序字段
		String orderType = c.getPara(PageConst.ORDER, "");// 获取排序方式
		if (!xx.isEmpty(orderField)) {
			sql = " order by " + orderField + ' ' + orderType;
		}

		return sql;
	}

	public static String getSort(Controller c) {
		return getSort(c, null);
	}

	/**
	 * 获取条件
	 *
	 * @param c
	 * @param eo
	 * @param eis
	 * @param parmList SQL参数
	 * @return
	 */
	public static String getWhere(Controller c, List<MetaField> eis, List<Object> parmList, String where) {

		// 动态解析变量和逻辑运算
		where = DynamicParse.buildSql(where, c.getSessionAttr(EovaConst.USER));

		StringBuilder sb = new StringBuilder();

		boolean isWhere = true;
		for (MetaField ei : eis) {
			// sql where 初始化
			if (isWhere) {
				// 存在初始过滤条件
				if (!xx.isEmpty(where)) {
					// 补where
					if (!where.toLowerCase().contains("where")) {
						sb.insert(0, " where ");
					} else {
						sb.append(" ");
					}
					sb.append(where + " ");
				} else {
					sb.append(" where 1=1 ");
				}
				isWhere = false;
			}

			String key = ei.getEn();
			// 给查询表单添加前缀，防止和系统级别字段重名
			String value = c.getPara(PageConst.QUERY + key, "").trim();
			String start = c.getPara(PageConst.START + key, "").trim();
			String end = c.getPara(PageConst.END + key, "").trim();
			
			
			String type = ei.getStr("type");
			//布尔类型会查询会出现 是条件但是未送
			if(type.equals(MetaField.TYPE_BOOL) && ei.getBoolean("is_query") && value == ""){
				value = "false";
			}
			
			// 当前字段 既无文本值 也无范围值，说明没填，直接跳过
			if ((xx.isEmpty(value) || value.equals("-1")) && xx.isAllEmpty(start, end)) {
				continue;
			}
			// 范围值只填一个，默认两个值相同
//			if (xx.isEmpty(start))
//				start = end;
//			if (xx.isEmpty(end))
//				end = start;
			
			
			
			// 布尔框需要转换值
			value = TemplateUtil.convertValue(ei, value,-1).toString();

			
			if (ei.getDataTypeName().toLowerCase().contains("int") && !xx.isEmpty(value)) {
				// 数字类型都是精确查询
				sb.append(" and " + key + " = ?");
				parmList.add(value);
			} else if (type.equals(MetaField.TYPE_TEXT) || type.equals(MetaField.TYPE_TEXTS) || type.equals(MetaField.TYPE_EDIT)) {
				// 文本类都是模糊条件
				sb.append(" and " + key + " like ?");
				parmList.add("%" + value + "%");
			} else if (type.equals(MetaField.TYPE_NUM)) {
				// 数值类的都是精准查询
				String condition = c.getPara(PageConst.COND + key, "").trim();

//				if (condition.equals("=")) {
//					sb.append(" and ? = " + key);
//					parmList.add(start);
//				} else if (condition.equals("<")) {
//					sb.append(" and ? > " + key);
//					parmList.add(start);
//				} else if (condition.equals(">")) {
//					sb.append(" and ? < " + key);
//					parmList.add(start);
//				} else {
//					// 只选一个值无效
//					if (xx.isOneEmpty(start, end)) {
//						continue;
//					}
				if (!xx.isEmpty(start)){
					sb.append(" and "+key+">=?  "   );
					parmList.add(start);
				}
			
				if (!xx.isEmpty(end)){
					sb.append(" and "+key+"<?  "   );
					parmList.add(end);
				}
				
				
//				}

				// 时间类的都是范围查询
			} else if (type.equals(MetaField.TYPE_TIME) || type.equals(MetaField.TYPE_DATE)) {
				// 只选一个时间无效
//				if (xx.isOneEmpty(start, end)) {
//					continue;
//				}
				
				if (!xx.isEmpty(start)){
					if (xx.isOracle()) {
						sb.append(" and " + key + " >= to_date(?,'yyyy-mm-dd')  " );
					} else {
						sb.append(" and date(" + key + ") >= ? ");
						// sb.append(" and " + key + " between ? and ?");时间范围查询
					}
					parmList.add(start);
				}
				
				if (!xx.isEmpty(end)){
					if (xx.isOracle()) {
						sb.append("  and " + key + " < to_date(?,'yyyy-mm-dd')+1");
					} else {
						sb.append(" and date(" + key + ") < DATE_ADD(?,INTERVAL 1 DAY) ");
						// sb.append(" and " + key + " between ? and ?");时间范围查询
					}
					parmList.add(end);
				}
				
				
			} else {
				if (ei.getBoolean("is_multiple")) {
					// 多值条件
					sb.append(" and (");
					for (String val : value.split(",")) {
						if (!sb.toString().endsWith(" (")) {
							sb.append(" or ");
						}
						sb.append(key + " like ");
						sb.append("?");
						parmList.add('%' + val + '%');
					}
					sb.append(")");
				} else {
					// 单值条件
					sb.append(" and " + key + " = ?");
					parmList.add(value);
				}
			}

			// 保持条件值回显
			ei.put("value", value);
		}

		return sb.toString();
	}

	/**
	 * 构建翻译查询条件
	 *
	 * @param sql
	 */
	public static void buildWhere(StringBuilder sql) {
		// 没有where关键字(说明表达式没有带条件)，自动添加where头，方便后续跟 and id in(x,x,x)
		if (!sql.toString().toLowerCase().contains("where")) {
			sql.append(" where 1=1 ");
		}
		// 自动切断后续条件，翻译是根据当前页所有ID查询外表，不需要关心额外条件
		// eg. where id = ${user.id}
		// int index = sql.indexOf("where");
		// if (index != -1) {
		// // 删除所有条件
		// sql.delete(index + 5, sql.length());
		// sql.append(" 1=1 ");
		// }
	}

	/**
	 * 添加where条件
	 *
	 * @param se
	 * @param condition
	 * @return
	 */
	public static String addWhere(EovaExp se, String condition) {

		String select = se.simpleSelect;
		String from = se.from;
		String where = se.where;

		if (xx.isEmpty(where)) {
			where = " where " + condition;
		} else {
			where += " and " + condition;
		}

		return select + from + where;
	}

	public static void convertValueByExp(Controller c, List<MetaField> eis, List<Record> reList, String... excludeFields) {
		// 根据表达式翻译显示CN(获取当前字段所有的 查询结果值，拼接成 字符串 用于 结合表达式进行 in()查询获得cn 然后替换之)
		F1: for (MetaField ei : eis) {
			// 获取存在表达式的列名
			String en = ei.getEn();
			// 排除不需要翻译的字段
			if (!xx.isEmpty(excludeFields)) {
				for (String field : excludeFields) {
					if (field.equals(en)) {
						continue F1;
					}
				}
			}
			// 只翻译需要显示的字段
//			if (!ei.getBoolean("is_show")) {
//				continue;
//			}
			// 获取控件表达式
			String exp = ei.getStr("exp");
			if (xx.isEmpty(exp)) {
				continue;
			}
			// System.out.println(en + " EovaExp:" + exp);
			// in 条件值
			Set<String> ids = new HashSet<String>();
			if (!xx.isEmpty(reList)) {
				for (Record re : reList) {
					String value = re.get(en, "").toString();
					if("".equals(value)){
						// 如果是空值直接continue
						continue;
					}
					if (value.contains(",")) {
						// 多值
						for (String val : value.split(",")) {
							ids.add(val);
						}
					} else {
						// 单值
						ids.add(value);
					}
				}
			}else
				break;//无数据不需要转意   392行

			exp = DynamicParse.buildSql(exp, c.getSessionAttr(EovaConst.USER));

			EovaExp se = new EovaExp(exp,true);
			String select = se.simpleSelect;
			String where = se.where;
			String from = se.from;
			String pk = se.pk;
			String cn = se.cn;

			// 清除value列查询条件，防止干扰翻译SQL条件
			where = filterValueCondition(where, pk);
			// PS:底部main有测试用例

			StringBuilder sql = new StringBuilder();
			sql.append(select);
			sql.append(from);
			sql.append(where);
			// 构建特殊翻译查询条件
			buildWhere(sql);

			// 查询本次所有翻译值
			if (!xx.isEmpty(ids)) {
				sql.append(" and ").append(pk);
				sql.append(" in(");
				// 根据当前页数据value列查询外表name列
				for (String id : ids) {
//					if(id!=null && "".equals(id)){
//						continue;
//					}
					//System.out.println("id: "+id);
					// TODO There might be a SQL injection risk warning
					sql.append(xx.format(id)).append(",");
				}
				sql.deleteCharAt(sql.length() - 1);
				sql.append(")");
			}

			List<Record> translates = Db.use(se.ds).find(sql.toString());

			// 翻译匹配项
			for (Record re : reList) {
				Object o = re.get(en);
				// 空字段无法翻译
				if (o == null) {
					re.set(en, "");
					continue;
				}

				String value = o.toString();

				String text = "";
				if (value.contains(",")) {
					// 多值
					for (String val : value.split(",")) {
						text += translateValue(pk, cn, translates, val);
						text += ',';
					}
					text = xx.delEnd(text, ",");
				} else {
					text = translateValue(pk, cn, translates, value);
				}
				re.set(en, text);
			}
		}
	}
	
	/**
	 * 非空字段转意（cn=》en）
	 * 目前导入用（如果转意不了保留原值）
	 * @param c
	 * @param eis -字段们
	 * @param reList -数据Records
	 * @param excludeFields
	 */
	public static void convertCNByExp(Controller c, List<MetaField> eis, List<Record> reList, String... excludeFields) {
		// 根据表达式翻译显示CN(获取当前字段所有的 查询结果值，拼接成 字符串 用于 结合表达式进行 in()查询获得cn 然后替换之)
		F1: for (MetaField ei : eis) {
			// 获取存在表达式的列名
			String en = ei.getEn();
			// 排除不需要翻译的字段
			if (!xx.isEmpty(excludeFields)) {
				for (String field : excludeFields) {
					if (field.equals(en)) {
						continue F1;
					}
				}
			}
			// 只翻译需要显示的字段
//			if (!ei.getBoolean("is_show")) {
//				continue;
//			}
			// 获取控件表达式
			String exp = ei.getStr("exp");
			if (xx.isEmpty(exp)) {
				continue;
			}
			// System.out.println(en + " EovaExp:" + exp);
			// in 条件值
			Set<String> ids = new HashSet<String>();
			if (!xx.isEmpty(reList)) {
				for (Record re : reList) {
					String value = re.get(en, "").toString();
					if(xx.isEmpty(value)){
						// 如果是空值直接continue
						continue;
					}
					if (value.contains(",")) {
						// 多值
						for (String val : value.split(",")) {
							ids.add(val);
						}
					} else {
						// 单值
						ids.add(value);
					}
				}
			}

			exp = DynamicParse.buildSql(exp, c.getSessionAttr(EovaConst.USER));

			EovaExp se = new EovaExp(exp,true);
			String select = se.simpleSelect;
			String where = se.where;
			String from = se.from;
			String pk = se.pk;
			String cn = se.cn;

			// 清除value列查询条件，防止干扰翻译SQL条件
			where = filterValueCondition(where, pk);
			// PS:底部main有测试用例

			StringBuilder sql = new StringBuilder();
			sql.append(select);
			sql.append(from);
			sql.append(where);
			// 构建特殊翻译查询条件
			buildWhere(sql);

			// 查询本次所有翻译值
			if (!xx.isEmpty(ids)) {
				sql.append(" and ").append(cn);
				sql.append(" in(");
				// 根据当前页数据value列查询外表name列
				for (String id : ids) {
//					if(id!=null && "".equals(id)){
//						continue;
//					}
					//System.out.println("id: "+id);
					// TODO There might be a SQL injection risk warning
					sql.append(xx.format(id)).append(",");
				}
				sql.deleteCharAt(sql.length() - 1);
				sql.append(")");
			}

			List<Record> translates = Db.use(se.ds).find(sql.toString());

			// 翻译匹配项
			for (Record re : reList) {
				Object o = re.get(en);
				// 空字段无法翻译
				if (xx.isEmpty(o)) {
					re.set(en, null);
					continue;
				}

				String showValue = o.toString();

				String text = "";
				if (showValue.contains(",")) {
					// 多值
					for (String showVal : showValue.split(",")) {
						text += translateShowValue(pk, cn, translates, showVal);
						text += ',';
					}
					text = xx.delEnd(text, ",");
				} else {
					text = translateShowValue(pk, cn, translates, showValue);
				}
				re.set(en, text);
			}
		}
	}

	/**
	 * 将value翻译text
	 *
	 * @param pk value
	 * @param cn text
	 * @param translates 字典集合
	 * @param re 待翻译对象
	 * @param value
	 */
	public static String translateValue(String pk, String cn, List<Record> translates, String value) {
		for (Record r : translates) {
			// 翻译前的值(默认为第1列查询值)
			String key = r.get(pk).toString();
			// 翻译后的值(默认为第2列查询值)
			String name = r.get(cn).toString();
			if (value.equals(key)) {
				return name;
			}
		}
		return value;
	}
	
	/**
	 *  将 text翻译成value
	 * @param pk
	 * @param cn
	 * @param translates
	 * @param value
	 * @return
	 */
	public static String translateShowValue(String pk, String cn, List<Record> translates, String showValue) {
		for (Record r : translates) {
			// 翻译前的值(默认为第1列查询值)
			String key = r.get(pk).toString();
			// 翻译后的值(默认为第2列查询值)
			String name = r.get(cn).toString();
			if (showValue.equals(name)) {
				return key;
			}
		}
		return showValue;
	}

	/**
	 * 通过Form构建数据
	 * 需要特殊处理的字段类型：时间（两种情况：有完整值完全写入，有空值''则意味着系统需要帮他初始化now）、布尔框（ 好像处理不了啊，on的时候能收到值，否则收不到 在非安全模式收不到暂时不予处理吧，如果出现设置需要把类似状态设置 false，暂时不会生效，推荐使用删除接口）
	 * @param c 控制器
	 * @param eis 对象属性
	 * @param record 当前操作对象数据集
	 * @param pkName 主键字段名
	 * @return 视图表数据分组
	 */
	public static Map<String, Record> buildData(Controller c, MetaObject object, Record record, String pkName, boolean isInsert) {
		Map<String, Record> datas = new HashMap<String, Record>();

		//安全模式（默认是安全模式），主要是在更新模式下：安全模式只提取可更新的字段，设置成非安全模式主要原因是更新可能由多种更新（标准更新：a,b,c,d字段），但是其他业务场景比如审核 则需要其他的字段：e，f
		//所以安全模式只更新abcd字段，非安全模式则由提交参数者确定~~
		Integer safeMode = object.getInt("safe_mode");
		if(safeMode == null)
			safeMode = 1;
		
		String ds = object.getDs();
//		 String dsType =  EovaConfig.MAIN_DBTYPE;
//		 if(object.getDs().equals("eova") || StringUtils.isEmpty(object.getDs()))
//			 dsType =  EovaConfig.EOVA_DBTYPE;
		  
		// 获取字段当前的值
		for (MetaField item : object.getFields()) {
			// System.out.println(item.getEn() +'='+ c.getPara(item.getEn()));
			String type = item.getStr("type");// 控件类型
			String key = item.getEn();// 字段名
			String data_type_name =(String) item.get("data_type_name");
			String pk = object.getPk();// 控件类型
			
			int dataSize = item.getDataSize();
			
			// 获当前字段 Requset Parameter Value，禁用=null,不填=""
			Object value = c.getPara(key);
			
			if(isInsert){//新增
				if(xx.isMssql(ds)){
					if(key.equalsIgnoreCase(pk) && xx.isEmpty(value)){
							if("uniqueidentifier".equalsIgnoreCase(data_type_name)){
								value = UUID.randomUUID().toString();
							}else if(("varchar".equalsIgnoreCase(data_type_name) || "nvarchar".equalsIgnoreCase(data_type_name)) && dataSize == 36){
								value = UUID.randomUUID().toString();
							}
					}else if("DATETIME".equalsIgnoreCase(data_type_name) && key.toLowerCase().indexOf("create")!=-1){
						value = DateUtil.format(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS);
					}else if("DATETIME".equalsIgnoreCase(data_type_name) && key.toLowerCase().indexOf("modify")!=-1){
						value = null;
					}
				
				}
			}else{//更新
				if(xx.isMssql(ds)){
//					 if("DATETIME".equalsIgnoreCase(data_type_name) && key.toLowerCase().indexOf("modify")!=-1){
//						value = DateUtil.format(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS);
//					 }else if("DATETIME".equalsIgnoreCase(data_type_name) && key.toLowerCase().indexOf("create")!=-1){
//						value = null;
//					 }
				}
			}
			

			
			
			// 值的预处理
			value = TemplateUtil.convertValue(item, value,safeMode);
			
			// 自动类型转换
			value = MetaDataType.convert(item, value,ds);
			// System.out.println(String.format("FormData：%s=%s", key, value));
			// 新增跳过自增长字段(新增时隐藏)
			if (value == null && type.equals(MetaField.TYPE_AUTO)) {
				continue;
			}
			
			//0-正常，10-只读，20-隐藏，50-禁用 ，禁用的情况下值无效（非安全模式也提取）
			if(isInsert){
				int addStatus = item.getInt("add_status");
				if(addStatus == 50 && safeMode == 1){
					continue;
				}
			}else{
				int updateStatus = item.getInt("update_status");
				if(updateStatus == 50 && safeMode == 1){
					continue;
				}
			}
			
			
			
			
			
			
			
			
			// 新增时，移除禁止新增的字段
//			 boolean isAdd = item.getBoolean("is_add");
//			 if (isInsert || isAdd) {
//				 //record.remove(key);
//			     //continue;
//				 
//				 if( "uniqueidentifier".equals(data_type_name) 
//							&& StringUtils.isEmpty((String)value) 
//							&& key.equalsIgnoreCase(pk) ){
//						value = UUID.randomUUID().toString();
//					}
//			 }
			 
			 
			// 当前字段为空(禁用/隐藏/非字符并未填写 且没有默认值),说明不需要持久化，直接跳过
				if (value == null) {
					continue;
				}
			 
			// 更新时，移除禁止更新的字段
			// boolean isUpdate = item.getBoolean("is_update");
			// if (!isInsert && !isUpdate) {
			// record.remove(key);
			// continue;
			// }

			// 全量的数据,mssql字段全部添加"[]",主键不修改，jfinal后面会校验
//			if(!object.getDs().equals("eova") && !object.getPk().equals(key))
//				record.set("["+key+"]", value);
//			else
				record.set(key, value);
		}
		return datas;
	}

	/**
	 * 视图持久化操作（视图中的表，目前不支持联合主键）
	 * 需要配置 config -》view字段见详细说明
	 * 支持跨数据库视图：但是数据库名得是：main/eova
	 * @param mode 操作模式 add/update/delete/delete_logical
	 * @param object 元对象
	 * @param data 视图当前操作所有数据
	 */
	@SuppressWarnings("rawtypes")
	public static void operateView(String mode, MetaObject object, Record data) {
		MetaObjectConfig config = object.getConfig();
		LinkedHashMap<String, TableConfig> view = config.getView();
		
		//最好查询下，比较怕data 数据未全部送上来（确认前端送过来的数据是完整的可以不执行此查询）
		if(mode.equals(TemplateConfig.DELETE) || mode.equals(TemplateConfig.DELETE_LOGICAL)){
			data = Db.use(object.getDs()).findById(object.getStr("view_name"), object.getPk(),data.get(object.getPk()));
		}
		
		Iterator iter = view.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String tableName = (String) entry.getKey();
			TableConfig tc = (TableConfig) entry.getValue();
			
			String ds = object.getDs();
			if(tableName.indexOf(".") != -1){
				ds = tableName.substring(0, tableName.indexOf("."));
				tableName = tableName.substring(tableName.indexOf(".")+1);
			}
				
			
			// 当前表的条件字段
			String whereField = tc.getWhereField();
			// 当前表的条件字段对应字段
			String paramField = tc.getParamField();

			// 按表获取持久化对象
			// Record po = recordGroup.get(table);
			Record po = getRecordByTableName(object, data, tableName);
			if (po == null) {
				continue;
			}

			// 主键可能并不是显示字段，所以需要手工获取关联字段的值
			if (!po.getColumns().containsKey(whereField)) {
				po.set(whereField, data.get(paramField));
			}

			if (mode.equals(TemplateConfig.UPDATE)) {
				// 按本表条件的关联字段进行更新
				Db.use(ds).update(tableName, whereField, po);
			} else if (mode.equals(TemplateConfig.ADD)) {
				// 按本表条件的关联字段进行更新（新增模式可能会带来 自增的一些值，需要回写到data，其他对象可能需要 ，尤其是自增主见可能是别人的外键，其他的什么默认值（时间、状态等字段）倒是无所谓）
				Db.use(ds).save(tableName, po);
				
				//主键不管之前有没有重新设置进 data
				Object key = po.get(whereField);
				if(key != null && data.get(paramField) == null){
					data.set(paramField, key);
				}
				System.out.println(po);
			}else if(mode.equals(TemplateConfig.DELETE)){
				//不支持联合主键
				Db.use(ds).delete(tableName, whereField, po);
				
			}else if(mode.equals(TemplateConfig.DELETE_LOGICAL)){//逻辑处理(如果无逻辑字段则直接物理操作)
				String hideFieldName = xx.getConfig("hide_field_name", "is_hide");
				
				if(po.getColumns().containsKey(hideFieldName)){//是否存在逻辑字段
					String sql = String.format("update %s set %s = 1 where %s = ?", tableName, hideFieldName, whereField);
					Db.use(ds).update(sql, data.get(paramField));
				}else
					Db.use(ds).delete(tableName, whereField, po);
			}
		}
	}

	/**
	 * 根据表名从当前操作数据中构建该表所属字段数据
	 * 有问题（data中字段值不一定和表中一致，推荐视图中不要做重命名，否则对应关系会失败）
	 * @param object 元数据
	 * @param data 当前操作数据集
	 * @param tableName 当前持久化表名
	 * @return 当前持久化数据对象
	 */
	private static Record getRecordByTableName(MetaObject object, Record data, String tableName) {
		List<MetaField> fields = object.getFields();
		Record r = null;
		for (MetaField f : fields) {
			if (tableName.equalsIgnoreCase(f.getStr("table_name"))) {
				String en = f.getEn();
				Object val = data.get(en);
				if (val == null)
					continue;
				if (r == null)
					r = new Record();
				r.set(en, val);
			}
		}
		return r;
	}

	/**
	 * 过滤指定查询条件
	 *
	 * @param where 查询条件Sql
	 * @param colName 要过滤的列名
	 * @return 过滤后的Sql
	 */
	public static String filterValueCondition(String where, String colName) {
		if (where.contains(colName)) {
			where = where.replaceAll("( " + colName + ".*?)and", "");
		}
		return where;
	}

	/**
	 * 获取关联参数‘ref=query_hotel_id:1,a:2,b:3’
	 *
	 * @param c
	 * @return
	 */
	public static Record getRef(Controller c) {
		Record r = new Record();

		try {
			String ref = c.getPara("ref");
			if (xx.isEmpty(ref)) {
				return r;
			}
			String[] fields = ref.split(",");
			for (String field : fields) {
				String[] strs = field.split(":");
				r.set(strs[0], strs[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return r;
		}

		return r;
	}

	/**
	 * 构建关联参数值
	 *
	 * @param object
	 */
	public static void buildRef(Controller ctrl, MetaObject object) {
		for (MetaField ei : object.getFields()) {
			String key = ei.getEn();

			Record ref = getRef(ctrl);
			if (ref != null && !xx.isEmpty(ref.get(key))) {
				ei.put("value", ref.get(key));
				ei.put("is_disable", true);
			}
		}
	}
	
	/**
	 * 设置默认值（最终目标是根据 viewConfig来确定 viewConfig 需要且不允许设置（只读或者禁用）的只能系统设置默认值）
	 * 目前设置的原则：如果无以及存在beetl表达式则设置，否则不予处理（比较弱可能会有人强制参数攻击，后面viewConfig存在就可以不需要前段送由后端强制设置）
	 * 目前只有导入在用
	 * @param ctrl -PAGE_TYPE=add/update/detail/examine
	 * @param object 
	 * @param reList
	 * @param viewConfig
	 */
	public static void setRecordsDefault(Controller ctrl, MetaObject object,List<Record> reList,Map viewConfig) {
		for (Record re : reList) {
			setRecordDefault(ctrl,  object,re,viewConfig);
		}
	}
	/**
	 * 设置默认值（最终目标是根据 viewConfig来确定 viewConfig 需要且不允许设置（只读或者禁用）的只能系统设置默认值）
	 * 目前设置的原则：如果无以及存在beetl表达式则设置，否则不予处理（比较弱可能会有人强制参数攻击，后面viewConfig存在就可以不需要前段送由后端强制设置）
	 * @param ctrl -PAGE_TYPE=add/update/detail/examine
	 * @param object
	 * @param re
	 * @param viewConfig-配置的视图（如果存在） 
	 */
	public static void setRecordDefault(Controller ctrl, MetaObject object,Record re,Map viewConfig) {
		
		List<MetaField> eis = object.getFields() ;
				 for (MetaField ei : eis) {
							// 获取存在表达式的列名
							String en = ei.getEn();
							
							String defaulter = ei.get("defaulter");
							
							if(!xx.isEmpty(defaulter)){
								String value = re.get(en);
								if(xx.isEmpty(value)  ){//${user.grade}
									defaulter = getRecordDefault(ctrl,ei,re,viewConfig);
									if(defaulter != null)
										re.set(en, defaulter);
									
								}else if(defaulter.indexOf("${") != -1){
									defaulter = getRecordDefault(ctrl,ei,re,viewConfig);
									if(defaulter != null)
										re.set(en, defaulter);
								}
							}
							
				 }
	}
	

	/**
	 * 获取默认值
	 * 目前是 无值||存在beetl表达式 才来获取
	 * @param ctrl
	 * @param ei
	 * @param re
	 * @param viewConfig -目前不支持
	 * @return
	 */
	private static String getRecordDefault(Controller ctrl,MetaField ei,Record re,Map viewConfig){
		String defaulter = ei.get("defaulter");
		String value = re.get(ei.getEn());
		
		if(viewConfig != null){//目前没有实现（等后面视图定义实现了完善）
			//检查是否beetl默认值
			defaulter = DynamicParse.buildSql(defaulter, ctrl.getSessionAttr(EovaConst.USER));
		}else{
			String PAGE_TYPE = ctrl.getPara(EovaConst.PAGE_TYPE,"add");
			if(PAGE_TYPE.equals("add")){//新增需要默认值，修改需要么？暂定不设置吧
				
				if(ei.getInt("add_status") == 10 || ei.getInt("add_status") ==20 ){//0=正常，10=只读，20=隐藏，50=禁用
					//检查是否beetl默认值
					defaulter = DynamicParse.buildSql(defaulter, ctrl.getSessionAttr(EovaConst.USER));
				}else if(xx.isEmpty(value) && ei.getInt("add_status") != 50){//需要但是是空的
					//检查是否beetl默认值
					defaulter = DynamicParse.buildSql(defaulter, ctrl.getSessionAttr(EovaConst.USER));
				}else
					defaulter = null;
				
				
				
				
			}else{
				if(ei.getInt("update_status") == 10 || ei.getInt("update_status") ==20 ){//0=正常，10=只读，20=隐藏，50=禁用
					//检查是否beetl默认值
					defaulter = DynamicParse.buildSql(defaulter, ctrl.getSessionAttr(EovaConst.USER));
				}else{
					defaulter = null;
				}
				
				
			}
		}
		
		
		return defaulter;
		
	}
	
	public static void main(String[] args) {
		/*
		 * 清除value列查询条件，防止干扰翻译SQL条件 自定义SQL eg. where uid = ${user.id} and type = 3 自动翻译SQL eg. and uid in(1,2,3)
		 */
		// String where = "where uid = ${user.id} and type = 1 and state = 1";
		// String where = "where type = 2 and uid = ${user.id} and state = 2";
		String where = "where type = 3 and uid = ${user.id} and state = 3 and uid = 3 and uid in (1,2,3)";
		String pk = "uid";
		System.out.println(where);
		where = filterValueCondition(where, pk);
		System.out.println(where);

		// addwhere
		System.out.println(addWhere(new EovaExp("select * from users where 1=1"), "id = 1"));
		System.out.println(addWhere(new EovaExp("select id ID,name CN from users where a = 1"), "id = 1"));
	}
}