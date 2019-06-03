package com.oss.job;

import com.eova.common.utils.xx;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.quartz.JobExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 每天执行
 *
 * @author Jieven
 * @date 2014-7-7
 */
public class EveryDayJob extends AbsJob {

	@Override
	protected void process(JobExecutionContext context) {
		System.out.println("每日任务");
		//runEovaInit();
		
		initPri();
	}
	
	
	
	
//	   public static void runEovaInit()  {
//		   //配置库重置
//
//		   String init_file = xx.getConfig("init_file");
//			if(StringUtils.isEmpty(init_file)){
//
//				return;
//			}
//		   Db.use(xx.DS_EOVA).initSqlFile(init_file);
//
//		   System.out.println("执行初始化完成~~");
//	    }
	   
	   
 private void initPri(){
		   
		   List allCode = Db.use(xx.DS_EOVA).query("SELECT menu_code FROM eova_button WHERE id>=100 AND NAME='新增' AND bs IS NULL");

		  
		   for(int x=0;x<allCode.size();x++){
//			   Map oneCode = (Map)allCode.get(x);
//			   String menu_code =(String) oneCode.get("menu_code");
			   String menu_code =(String) allCode.get(x);
			   List<Record> list = Db.use(xx.DS_EOVA).find("select * from eova_button where menu_code=? order by id asc",menu_code);
			   
			   Record first = null;
			   int index = 0;
			   List<String> objs = new ArrayList();
			   for(int i=0;i<list.size();i++){
				   Record one =  list.get(i);
				   menu_code = one.get("menu_code");
				   String name = one.get("name");
				   if("查询".equalsIgnoreCase(name) && i == 0){
					   first = one;
//				   }
//				   
//				   if(first != null){
					   ///master_slave_grid/list/hotel_bs;/grid/query/hotel*;/grid/export/hotel*;/grid/query/hotel_bed*;/grid/export/hotel_bed*;/grid/query/hotel_stock*;/grid/export/hotel_stock*
					   String bs = first.get("bs");
					   String[] bss = bs.split(";");
					   String obj1 =  bss[1];
					   
					   for(int z=0;z<bss.length;z++){
						   if(bss[z].startsWith("/grid/query/")){
							   String tempObj = bss[z].replace("/grid/query/", "");
							   tempObj = tempObj.substring(0,tempObj.length() -1);
							   objs.add(tempObj);
						   }
					   }
					   
					   
				   }
				   
				   //第一组
				   if("新增".equalsIgnoreCase(name)){
					   index = 0;
				   }else if(name.endsWith("新增")){
					   index = index+1;
				   }
				   
				   
				   if(xx.isEmpty(one.get("bs"))){
					   if(name.endsWith("新增")){
						   ///form/add/eova_log_code*;/form/doAdd/eova_log_code
						   one.set("bs", "/form/add/"+objs.get(index)+"*;/form/doAdd/"+objs.get(index)+"");
					   }else if(name.endsWith("修改")){
						   ///form/update/eova_log_code*;/form/doUpdate/eova_log_code
						   one.set("bs", "/form/update/"+objs.get(index)+"*;/form/doUpdate/"+objs.get(index)+"");
					   }else if(name.endsWith("删除")){
						   ///grid/delete/eova_log_code
						   one.set("bs", "/grid/delete/"+objs.get(index));
					   }else if(name.endsWith("查看")){
						   ///form/detail/eova_log_code*
						   one.set("bs", "/form/detail/"+objs.get(index)+"*");
					   }
					   Db.use(xx.DS_EOVA).update("eova_button", one);
				   }
				   
			   }
			   
		   }
		   
		 
	   }
}
