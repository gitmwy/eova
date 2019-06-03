/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.core.button;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;

import com.eova.common.Easy;
import com.eova.common.Response;
import com.eova.common.utils.xx;
import com.eova.common.utils.db.DbUtil;
import com.eova.common.utils.io.FileUtils;
import com.eova.common.utils.net.HttpUtil;
import com.eova.common.utils.string.PinyinTool;
import com.eova.common.utils.util.HttpClientUtil;
import com.eova.common.utils.util.StringUtils;
import com.eova.config.EovaConst;
import com.eova.config.PageConst;
import com.eova.engine.DynamicParse;
import com.eova.model.Button;
import com.eova.model.MetaObject;
import com.eova.model.RoleBtn;
import com.eova.service.sm;
import com.eova.widget.form.FormController;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.NestedTransactionHelpException;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.activerecord.tx.TxConfig;

/**
 * 按钮管理
 * 
 * @author Jieven
 * @date 2014-9-11
 */
public class ButtonController extends Controller {

	// 菜单基本功能管理
	public void quick() {
		setAttr("menuCode", getPara(0));
		setAttr("objectCode", getPara(1));
		
		String template = this.getPara(PageConst.Template);//模板
		if (!xx.isEmpty(template)) 
			render("/eova/button/quick_"+template+".html");
		else
			render("/eova/button/quick.html");
		
	}

	// 菜单基本功能管理
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void doQuick() {
		try {
			Button btn = new Button();
			String menuCode = getPara("menu_code");
			btn.set("menu_code", menuCode);
			Integer groupNum = getParaToInt("group_num", 0);
			btn.set("group_num", groupNum);
			btn.set("icon", getPara("icon"));
			btn.set("name", getPara("name"));
			btn.set("ui", getPara("ui"));
			btn.set("bs", getPara("bs"));
			// 计算最大排序值
			btn.set("order_num", Button.dao.getMaxOrderNum(menuCode, groupNum) + 1);
			btn.save();

			// 分配权限
			String roles = getPara("role", EovaConst.ADMIN_RID + "");
			for (String role : roles.split(",")) {
				RoleBtn rb = new RoleBtn();
				rb.set("rid", role);
				rb.set("bid", btn.get("id"));
				rb.save();
			}
			
			renderJson(new Easy());
		} catch (Exception e) {
			e.printStackTrace();
			renderJson(new Easy("新增按钮失败,请看控制台日志寻找原因！"));
			throw new NestedTransactionHelpException("新增按钮失败");
		}
	}
	
	/**
	 * 从基准按钮copy形成一个新的按钮供修改，需要形成：btn下的按钮"功能.html",以及外一层的“功能.html”
	 * 如果文件存在则覆盖，数据库存在则不处理
	 * @throws Exception
	 */
	@Before(Tx.class)
	@TxConfig(xx.DS_EOVA)
	public void copyBtn() throws Exception {
		String menuCode = this.getPara(0);
		String objectCode = this.getPara(1);

		String name = this.getPara("srcName");
		//String baseUi = this.getPara("base");
		String newName = this.getPara("newName");
		String icon = this.getPara("icon");//图标（如果设置的话）
		String form_code = this.getPara("form_code");//模板form名
		
		String isNewPage = this.getPara("isNewPage", "1");//是否生成新页面以修改，默认生成
		
		//根路径/菜单名/对象名/
		String baseUi = xx.getConfig("page_default", "/")+menuCode+"/"+objectCode+"/";

		
		Button record = Button.dao.findFirst("SELECT * FROM eova_button b WHERE b.menu_code=? AND b.name LIKE ? order by id", menuCode,"%"+name+"%");
		MetaObject object = sm.meta.getMeta(objectCode);
		
		///eova/template/single/btn/add_lay.html
		String srcUi = record.getStr("ui");
		
		String path = this.getRequest().getServletContext().getRealPath("/"); 
		System.out.println(path);
		
		//File dst = new File(path+File.separator+baseUi);
		//FileUtils.copy(src, dst);
		
		//修改src文件内容并写入(修改title 以及 类额外属性)
		//String content = FileUtils.readFile(new FileInputStream(src));
		String content = FileUtils.readFileAll(path+File.separator+srcUi);
		content =	content.replace("${button.name}", newName);
		
		String newNamePinyin = PinyinTool.getInstance().toPinYinLower(newName);
		String toBtnUrl = baseUi+"btn"+"/"+newNamePinyin+".html";
		
		int p = 0;
		int end = 0;
		if(isNewPage.equalsIgnoreCase("1")){//生成静态按钮，需要静态按钮内容
			
			
			p = content.indexOf("${button.icon");
			end = content.indexOf("}", p+"${button.icon".length());
			//content =	content.substring(0, p) + record.getStr("icon")+content.substring(end+1);//动态图标的保留吧
			
			content =	content.replace("${ZN2Pinyin(button.name!)}", newNamePinyin);
			
			
			
			p = content.indexOf("var url =");
			end = content.indexOf(";",p);
			int aferVarP = end;
			//添加页面标记aferVarP 后添加
			String add ="url = url+'&menuCode="+menuCode+"&page="+newNamePinyin+"';";
			content = content.substring(0,aferVarP+1)+add+content.substring(aferVarP+1);
			
			
			///eova/template/single/btn/add_lay.html
			//String toBtnUrl = baseUi+"btn"+"/"+newNamePinyin+".html";
			String pageDefaultOver = xx.getConfig("page_default_over", "0");
			if("0".equals(pageDefaultOver)) {
				File btnFile = new File(path+File.separator+toBtnUrl);
				if(btnFile.exists())
					renderJson(new Response(Response.STATUS_FAIL,"路径下文件存在，创建文件失败"));
					return ;
			}
		
		}else{
			//<button class="layui-btn layui-btn-sm ${ZN2Pinyin(button.name!)}${object.code}" form="" title="${button.name}">
			//form="" ==》 form="form_code"
			content =	content.replace("form=\"\"", "form=\""+form_code+"\"");
		}
		FileUtils.makeDirectory(new File(path+toBtnUrl));
		FileUtils.SaveFileAs(content, path+File.separator+toBtnUrl);
				
		if(isNewPage.equalsIgnoreCase("1")){//生成静态页面
			
		
			//2、提取 var url = '/form/update/demo2_test-' + data[0].pk_id+'?template=lay' || /form/add/${object.code}?template=lay;并且去掉"+ data[0].pk_id+
			String url = content.substring(p+"var url =".length(), end);
			
			
			p = url.indexOf("'");
			end = url.indexOf("'",p+1);
			String  urlpre = url.substring(p+1,end);
			
			p = url.indexOf("'",end+1);
			if(p != -1){
				end = url.indexOf("'",p+1);
				String  urlafter = url.substring(p+1,end);
				url = urlpre+urlafter;
			}else{
				url = urlpre;
			}
			
			
			
			
			//添加模板
			if(!xx.isEmpty(form_code)) {
				url = url+"&"+FormController.VIEW_FORM+"="+form_code;
			}
			
			url = url.replace("object", "metaobject");
			url = DynamicParse.buildSql(url, object);
			//String newNamePinYin = PinyinTool.getInstance().toPinYinLower(newName);
			
			//3、获取文件
			String getUrl =  this.getRequest().getRequestURL().toString()
					.replace(this.getRequest().getRequestURI(), "")
					+url;
			
			
			String cookies = ReadCookie(this.getRequest());
			String result = HttpClientUtil.getInstance().get(getUrl,cookies);
			//System.out.println(result);
			//页面中有部分静态量需要处理
			result = staticValueHandle(result);
			
			String toUrl = baseUi+"/"+newNamePinyin+".html";
			FileUtils.SaveFileAs(result, path+File.separator+toUrl);
		}
		
		//4、记录数据库（检查是否生成过）
		Button newRecord = Button.dao.findFirst("SELECT * FROM eova_button b WHERE b.menu_code=? AND b.name = ? order by id", menuCode,newName);
		if(newRecord == null){
			Button btn = new Button();
			//menuCode = getPara("menu_code");
			btn.set("menu_code", menuCode);
			Integer groupNum = getParaToInt("group_num", 0);
			btn.set("group_num", groupNum);
			if(!xx.isEmpty(icon))
				btn.set("icon", icon);
			btn.set("name", newName);
			btn.set("ui", toBtnUrl);
			btn.set("bs",record.getStr("bs"));
			// 计算最大排序值
			btn.set("order_num", Button.dao.getMaxOrderNum(menuCode, groupNum) + 1);
			btn.save();

			// 分配权限
			String roles = getPara("role", EovaConst.ADMIN_RID + "");
			for (String role : roles.split(",")) {
				RoleBtn rb = new RoleBtn();
				rb.set("rid", role);
				rb.set("bid", btn.get("id"));
				rb.save();
			}
		}
		
		
		
		renderJson(new Response(toBtnUrl));
	}

	// 导出选中按钮数据
	public void doExport() {
		String ids = getPara(0);

		StringBuilder sb = new StringBuilder();

		String sql = "select * from eova_button where id in (" + ids + ")";
		List<Record> objects = Db.use(xx.DS_EOVA).find(sql);
		DbUtil.generateSql(objects, "eova_button", "id", sb);

		renderText(sb.toString());
	}
	
	
	
	
    /**
     * 将cookie封装到Map里面
     * 
     * @param request
     * @return
     */
    private  Map<String, Cookie> ReadCookieMap(HttpServletRequest request) {
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        Cookie[] cookies = request.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie);
            }
        }
        return cookieMap;
    }
    
    private  String ReadCookie(HttpServletRequest request) {
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        Cookie[] cookies = request.getCookies();
        StringBuffer sb = new StringBuffer();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie);
                if(sb.length() != 0)
                	sb.append(";");
                sb.append(cookie.getName()+"="+cookie.getValue());
            }
        }
        return sb.toString();
    }
    
    private static final String STANDARD_FILE = "/eova/include_lay.html";
    /**
     * 静态量需要处理
     * @param content
     */
    private String staticValueHandle(String content){
//    	// 全局JS常量配置
//    	var IMG = "http://127.0.0.1:8080/file";
//    	var FILE = "http://127.0.0.1:8080/file";
//    	var FILE_MAX = "5120";
//    	var IMG_MAX = "5120";
//
//    	//类似 .jpg|.gif|.png|.bmp
//    	var UPLOAD_IMG_TYPE = ".jpeg|.jpg|.gif|.png|.bmp".replaceAll('\\.', '');
//    	var UPLOAD_FILE_TYPE = ".jpg|.jepg|.gif|.png|.bmp|.gz|.7z|.rar|.zip|.swf|.mp3|.mp4|.jar|.apk|.ipa|.doc|.docx|.xls|.xlsx|.ppt|.pptx|.pdf|.txt".replaceAll('\\.', '');
//    	//刷新此页面
//    	var refresh = function() {
//    		location.reload();
//    	};
//
//    	//页面类型 add/update/detail/examine
//    	var PAGE_TYPE = "update";
    	
    	String path = this.getRequest().getServletContext().getRealPath("/"); 
    	
    	
    	
    	List<String> standardLines = FileUtils.readFileByLines(path+STANDARD_FILE);
    	for(String line:standardLines){
    		//line
    		//var PAGE_TYPE = "update";
    		if(!xx.isEmpty(line) && line.startsWith("var")){
    			String temp = line.replaceFirst("var", "").trim();
    			if(temp.indexOf("=")  != -1  
    					&& StringUtils.testAllUpperCase( temp.substring(0, temp.indexOf("=")).trim())){//=之前的是大写
    				//如果存在则提取出来，替换content的内容
    				String target = line.substring(0,line.indexOf("="));
    				
    				int p = content.indexOf(target);
    				int end = content.indexOf(";", p);
    				if(p != -1 && end  != -1){
    					
    					//content = content.replaceFirst(content.substring(p, end), line);
    					
    					content = content.substring(0,p)+line+content.substring(end+1);
    				}
    			}
    		}
    	}
    	
    	return content;
    }
   
	
	public static void main(String [] args){
		String content = "title=\"${button.name}\"${button.name}";
		content =	content.replace("${button.name}", "宏观");
		//content.replaceAll(regex, replacement)
		content = "  s b ";
		System.out.println(content.trim());
		
		String scriptResult ="";//脚本的执行结果

		
		String routeScript = "var a= '123';";
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");//1.得到脚本引擎
        
        System.out.println(scriptResult.toString());
        
        
        
        System.out.println(StringUtils.testAllUpperCase("AB "));
	}

}