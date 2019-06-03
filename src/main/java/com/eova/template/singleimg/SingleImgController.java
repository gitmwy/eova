/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.template.singleimg;

import java.util.List;

import com.eova.aop.AopContext;
import com.eova.common.utils.xx;
import com.eova.common.utils.io.FileUtil;
import com.eova.config.EovaConst;
import com.eova.config.PageConst;
import com.eova.core.menu.config.MenuConfig;
import com.eova.model.Button;
import com.eova.model.EovaLog;
import com.eova.model.Menu;
import com.eova.model.MetaObject;
import com.eova.model.User;
import com.eova.service.sm;
import com.eova.template.common.util.TemplateUtil;
import com.eova.template.single.SingleController;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.upload.UploadFile;

/**
 * 业务模版：单图表(DataGrid)
 * 
 * @author jin
 * 
 */
public class SingleImgController extends SingleController {



	public void list() {

		String menuCode = this.getPara(0);

		// 获取元数据
		Menu menu = Menu.dao.findByCode(menuCode);
		MenuConfig config = menu.getConfig();
		String objectCode = config.getObjectCode();
		MetaObject object = MetaObject.dao.getByCode(objectCode);
//		List<MetaField> fields = MetaField.dao.queryByObjectCode(objectCode);

		// 根据权限获取功能按钮
		User user = this.getSessionAttr(EovaConst.USER);
		List<Button> btnList = Button.dao.queryByMenuCode(menuCode, user);

		// 是否需要显示快速查询
		setAttr("isQuery", MetaObject.dao.isExistQuery(objectCode));

		setAttr("menu", menu);
		setAttr("btnList", btnList);
		setAttr("object", object);
//		setAttr("fields", fields);
		
		
		
		formPageType("imglist");
		String template = this.getPara(PageConst.Template);//模板
		if (!xx.isEmpty(template)) 
			render("/eova/template/singleimg/list_"+template+".html");
		else
			render("/eova/template/singleimg/list.html");
		
	}
	
	
	/**
	 * 指定自身方法或者叫类型
	 * @param fun
	 */
	protected void formPageType(String fun){
		String page = this.getPara("page");
		if(!xx.isEmpty(page)){
			setAttr("PAGE_TYPE", page);
		}else{
			setAttr("PAGE_TYPE", fun);
		}	
	}

}