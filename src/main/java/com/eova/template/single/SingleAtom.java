package com.eova.template.single;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.eova.aop.AopContext;
import com.eova.common.utils.xx;
import com.eova.common.utils.excel.ExcelUtil;
import com.eova.model.MetaObject;
import com.eova.template.common.config.TemplateConfig;
import com.eova.widget.WidgetManager;
import com.eova.widget.WidgetUtil;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;

import jxl.Workbook;

/**
 * 目前只支持单表的导入（新增or 更新）
 * 视图的导入 a left join b （新增可以支持，但是更新存在只更新a或者b情况暂时不支持）所以导致视图的导入未实现,有需要的同学请自行修改 WidgetManager.operateView 以支持
 * 导入关于默认值也是蛋疼的事情：比如默认生效，默认导入人 还有更新模式和新增模式也有差异
 * =》目前简单的处理为只要空都设置，如果用户需要特殊处理，请自行拦截器处理之（系统处理后的数据再次处理一次）
 * @author jin
 *
 */
public class SingleAtom implements IAtom {

	private final Controller ctrl;
	private File file;
	private MetaObject object;
	private SingleIntercept intercept;
	private List<Record> records;
	private Exception runExp;
	//private 

	public SingleAtom(File file, MetaObject object, SingleIntercept intercept, Controller ctrl) {
		super();
		this.file = file;
		this.object = object;
		this.intercept = intercept;
		this.ctrl = ctrl;
	}

	@Override
	public boolean run() throws SQLException {
		InputStream is = null;
		Workbook workbook = null;
		try {
			int sheet = -1;
			int fisrtline = -1;
			Map<Integer,String> enColumn = null;
			
		
			sheet = ctrl.getParaToInt(ExcelUtil.IMP_EXCEL_DEFAULT_SHEET,-1);
			fisrtline = ctrl.getParaToInt(ExcelUtil.IMP_EXCEL_SHEET_FIRSTLINE,-1);
			
			boolean hasNext = false;
			// 导入前置任务
			if (intercept != null) {
							AopContext ac = new AopContext(ctrl, records,object);
							intercept.importBefore(ac);
							
							if(!xx.isEmpty(ctrl.getAttrForInt(ExcelUtil.IMP_EXCEL_DEFAULT_SHEET)))
								sheet =	ctrl.getAttrForInt(ExcelUtil.IMP_EXCEL_DEFAULT_SHEET);
							if(!xx.isEmpty(ctrl.getAttrForInt(ExcelUtil.IMP_EXCEL_SHEET_FIRSTLINE)))
								fisrtline =	ctrl.getAttrForInt(ExcelUtil.IMP_EXCEL_SHEET_FIRSTLINE);
							enColumn =	(Map)ctrl.getAttr(ExcelUtil.IMP_EXCEL_COLUMN2EN);
				hasNext = true;
			}
			
			// 导入Excel
			is = new FileInputStream(file);
			
			workbook =	ExcelUtil.openWorkbook(is);
			records = ExcelUtil.importExcel(ctrl,workbook, object.getFields(),sheet,fisrtline,enColumn,hasNext);
			
			if (xx.isEmpty(records)) {
				throw new Exception("无法从导入的Excel获取到任何数据，请认真检查Excel模版！");
			}
			
			// 备份Value列，然后将值列转换成Key列
			//WidgetUtil.copyValueColumn(page.getList(), object.getPk(), object.getFields());
			// 根据表达式将数据中的值翻译成原始值
			WidgetManager.convertCNByExp(ctrl, object.getFields(), records);
			// 设置默认值
			WidgetManager.setRecordsDefault(ctrl, object, records, null);
			
			// 导入前置任务
			if (intercept != null) {
				AopContext ac = new AopContext(ctrl, records,object);
				intercept.importInit(ac);
			}
			
			
			// 保存导入数据（部分字段需要反向转意）,以及默认值得处理
			for (Record re : records) {

				// TODO 拦截器 控制字段是否持久化，不需要的 比如 自增的 foreach remove 即可
				
				if(!xx.isEmpty(object.getTable())){
					//有主键是更新，没主键是新增
					Object pk = re.get(object.getPk());
					if (pk == null) {
						Db.use(object.getDs()).save(object.getTable(), object.getPk(), re);
					} else {
						Db.use(object.getDs()).update(object.getTable(), object.getPk(), re);
					}
				}else{
					// update view
					 WidgetManager.operateView(TemplateConfig.ADD, object, re);
					// 视图无法自动操作，请自定义元对象业务拦截完成持久化逻辑！
				}
				
				
				
				//WidgetManager.operateView(TemplateConfig.ADD, object, record);
				
				//if (!xx.isEmpty(object.getTable())) {
				//WidgetManager.operateView(TemplateConfig.ADD, object, record);
			}

			// 新增后置任务
			if (intercept != null) {
				AopContext ac = new AopContext(ctrl, records,object);
				intercept.importAfter(ac);
			}
		} catch (Exception e) {
			runExp = e;
			return false;
		} finally {
			
			if(workbook != null)
				workbook.close();
			
			if (is != null) try { is.close(); } catch (IOException e) { e.printStackTrace(); }
			// 自动删除废弃临时文件
			if (file.exists()) file.delete();
		}
		return true;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public MetaObject getObject() {
		return object;
	}

	public void setObject(MetaObject object) {
		this.object = object;
	}

	public SingleIntercept getIntercept() {
		return intercept;
	}

	public void setIntercept(SingleIntercept intercept) {
		this.intercept = intercept;
	}

	public List<Record> getRecords() {
		return records;
	}

	public void setRecords(List<Record> records) {
		this.records = records;
	}

	public Exception getRunExp() {
		return runExp;
	}

	public void setRunExp(Exception runExp) {
		this.runExp = runExp;
	}

}
