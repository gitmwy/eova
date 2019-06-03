/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.template.single;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.eova.aop.AopContext;
import com.eova.common.utils.excel.ExcelUtil;
import com.jfinal.plugin.activerecord.Record;

/**
 * 单表模版业务拦截器，菜单拦截<br>
 * 前置任务和后置任务(事务未提交)<br>
 * 成功之后(事务已提交)<br>
 * 
 * @author Jieven
 * @date 2014-8-29
 */
public class SingleIntercept {
	
	/**
	 * 
	 * 导入前置任务(事务内)
	 * 主要是设置excel一些参数（前2个参数运行前段传递，本处覆盖）：
	 * sheet =	ctrl.getAttrForInt(ExcelUtil.IMP_EXCEL_DEFAULT_SHEET);
	 * fisrtline =	ctrl.getAttrForInt(ExcelUtil.IMP_EXCEL_SHEET_FIRSTLINE);
	 * enColumn =	(Map)ctrl.getAttr(ExcelUtil.IMP_EXCEL_COLUMN2EN);
	 */
	public void importBefore(AopContext ac) throws Exception {
	}
	
	/**
	 * 需要处理用户信息（如果不存在的话）
	 * excel初始数据后前置任务(事务内)
	 * ac.ctrl.getAttr(ExcelUtil.IMP_EXCEL_SHEET); excel数据如果需要再次处理的话
	 * ac.ctrl.getAttr(ExcelUtil.IMP_EXCEL_SHEET_HEAD) 头信息
	 * Record.get(ExcelUtil.SHEET_ROWNO) 数据对应的 excel行号 注意：：：：：：如果有拦截器处理完业务需要删除掉ExcelUtil.SHEET_ROWNO属性，否则保存会报错
	 * ExcelUtil.getSheetRowNameValue 获取需要的列数据
	 * @param ac 
	 * @throws Exception
	 */
	public void importInit(AopContext ac) throws Exception {
		List<Record> records = ac.records;
		
		for(Iterator<Record> it =records.iterator();it.hasNext();) {
			Record one = it.next();
			
			one.remove(ExcelUtil.SHEET_ROWNO);
		}
	}
	

	/**
	 * 导入后置任务(事务内)
	 * 
	 */
	public void importAfter(AopContext ac) throws Exception {
	}

	/**
	 * 导入成功之后(事务外)
	 * 
	 */
	public void importSucceed(AopContext ac) throws Exception {
	}
}