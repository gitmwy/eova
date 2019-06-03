package com.eova.common.utils.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.eova.common.utils.xx;
import com.eova.common.utils.util.StringUtils;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

/**
 * Excel导入导出
 * jxl好像不支持xlsx，比较蛋疼，需要换成 poi
 * @author Jieven
 *
 */
public class ExcelUtil {
	public static final String IMP_EXCEL_SHEET = "IMP_EXCEL_SHEET";
	public static final String IMP_EXCEL_SHEET_HEAD = "IMP_EXCEL_SHEET_HEAD";
	public static final String IMP_EXCEL_DEFAULT_SHEET = "IMP_EXCEL_DEFAULT_SHEET";
	public static final String IMP_EXCEL_SHEET_FIRSTLINE = "IMP_EXCEL_SHEET_FIRSTLINE";
	public static final String IMP_EXCEL_COLUMN2EN = "IMP_EXCEL_COLUMN2EN";
	public static final String SHEET_ROWNO = "SHEET_ROWNO";

	
	//检查是否一行标准行检查列数量(前checkColumn数量)
	public static int checkColumn = 3;
	
    public static void createExcel(OutputStream os, List<Record> list, List<MetaField> items, MetaObject object) throws WriteException, IOException {
        // 创建工作薄
        WritableWorkbook workbook = Workbook.createWorkbook(os);
        // 创建新的一页
        WritableSheet sheet = workbook.createSheet("Sheet1", 0);

        int row = 0;// 当前行索引
        // 写入标题
        for (int i = 0; i < items.size(); i++) {
            MetaField item = items.get(i);
            WritableCellFormat format = new WritableCellFormat();
            format.setBackground(Colour.GRAY_25);// 设置灰色背景
            sheet.addCell(new Label(i, row, item.getCn(), format));
            // 设置列宽度(列宽px值/10)
            //sheet.setColumnView(i, item.getInt("width") / 10);
            
            // 设置列宽度(列宽px值/10)
            String width = item.getStr("width");
            
            if(StringUtils.isNumeric(width)){
            	sheet.setColumnView(i, Integer.parseInt(width)  / 10);
            }else{
            	//sheet.setColumnView(i, Integer.parseInt(width)  / 10);
            }
        }
        row++;
        // 写入数据行
        for (; row <= list.size(); row++) {
            Record record = list.get(row - 1);
            // 获取当前行数据
            String[] values = getValues(items, record);
            for (int i = 0; i < values.length; i++) {
                sheet.addCell(new Label(i, row, values[i]));
            }
        }

        // 把创建的内容写入到输出流中，并关闭输出流
        workbook.write();
        workbook.close();

    }

    /**
     * 获取数据行
     *
     * @param names
     * @param record
     * @return
     */
    private static String[] getValues(List<MetaField> items, Record record) {
        String[] values = new String[items.size()];
        int i = -1;
        for (MetaField item : items) {
            i++;
            Object value = record.get(item.getEn());
            if (value == null) {
                continue;
            }
            values[i] = value.toString();
        }
        return values;
    }
    
    public static Workbook openWorkbook(InputStream is) throws Exception  {
    	Workbook workbook = Workbook.getWorkbook(is);
    	return workbook;
    }
    /**
     * 读取excel
     * 需要确认的：sheet，fisrtline，enColumn还需要么？如果需要后面再补充吧
     * @param ctrl excel数据还得保存进去吧，后续有业务处理？
     * @param workbook excel已经打开 主要因为是关闭需要再后续操作中关闭 （只读即可）
     * @param items
     * @param sheet sheet=-1则默认为0
     * @param fisrtline 确定第一行 ，-1即为需要自行判断，标准：第一，第二列 不能空空？
     * @param enColumn n列对应的 en，可以空则自行判断(可以定义部分，因为部分不标准)
     * @param hassNext 是否有后续处理，如果没有则不会把excel的原始数据后传
     * @return
     */
    public static List<Record> importExcel(Controller ctrl,Workbook workbook, List<MetaField> items,int sheet,int fisrtline,Map<Integer,String> enColumn,boolean hasNext) {
    	if(sheet == -1)
    		sheet = 0;
        List<Record> list = new ArrayList<Record>();

        
        Sheet readsheet = null;
        try {
            // 从读取流创建只读Workbook对象
            //workbook = Workbook.getWorkbook(is);
            // 获取第一张Sheet表
            readsheet = workbook.getSheet(sheet);
            // 总列数
            int colSum = readsheet.getColumns();
            // 总行数
            int rowSum = readsheet.getRows();

            // 表头中文信息
            String[] headers = new String[colSum];
            // EN -> CN
            Map<String, String> field = getKeyValue(items);

            
            
            if(fisrtline == -1){
            	fisrtline = getFistLine(readsheet);
            }
            
            // 读取表头
            for (int i = 0; i < colSum; i++) {
                Cell cell = readsheet.getCell(i, fisrtline);
                headers[i] = StringUtils.TextValue(cell.getContents()) ;
            }
            int row = fisrtline+1;

            // 获取指定单元格的对象引用
            for (; row < rowSum; row++) {
            	boolean isline = isLine(readsheet,row);
            	if(!isline)
            		continue;
            	
                Record record = new Record();
                for (int i = 0; i < colSum; i++) {
                    Cell cell = readsheet.getCell(i, row);
                    String s = cell.getContents();
                    // 空值不参与导入持久化
                    if(xx.isEmpty(s))
                    	continue;
                    
                    String key = null;
                    if(enColumn != null){
                    	key = enColumn.get(i);
                    }
                    
                    if(key == null){
                    	// 根据表头CN获取EN
                    	key = field.get(headers[i]);
                    }
                
                	if(!xx.isEmpty(key)){
                		record.set(key, s);
                	}
                    
                }
                if(hasNext)
                	record.set(SHEET_ROWNO, row);//记录用，时候追溯数据
                list.add(record);
            }
            
            //可能需要后续处理
            if(hasNext) {
            	ctrl.setAttr(IMP_EXCEL_SHEET, readsheet);
                ctrl.setAttr(IMP_EXCEL_SHEET_HEAD, headers);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } 

        return list;
    }
    
    /**
     * 读取excel
     * 需要确认的：sheet，fisrtline，enColumn还需要么？如果需要后面再补充吧
     * @param ctrl excel数据还得保存进去吧，后续有业务处理？
     * @param is
     * @param items
     * @param sheet sheet=-1则默认为0
     * @param fisrtline 确定第一行 ，-1即为需要自行判断，标准：第一，第二列 不能空空？
     * @param enColumn n列对应的 en，可以空则自行判断(可以定义部分，因为部分不标准)
     * @return
     */
    public static List<Record> importExcel(Controller ctrl,InputStream is, List<MetaField> items,int sheet,int fisrtline,Map<Integer,String> enColumn) {
    	if(sheet == -1)
    		sheet = 0;
        List<Record> list = new ArrayList<Record>();

        Workbook workbook = null;
        Sheet readsheet = null;
        try {
            // 从读取流创建只读Workbook对象
            workbook = Workbook.getWorkbook(is);
            // 获取第一张Sheet表
            readsheet = workbook.getSheet(sheet);
            // 总列数
            int colSum = readsheet.getColumns();
            // 总行数
            int rowSum = readsheet.getRows();

            // 表头中文信息
            String[] headers = new String[colSum];
            // EN -> CN
            Map<String, String> field = getKeyValue(items);

            
            
            if(fisrtline == -1){
            	fisrtline = getFistLine(readsheet);
            }
            
            // 读取表头
            for (int i = 0; i < colSum; i++) {
                Cell cell = readsheet.getCell(i, fisrtline);
                headers[i] = StringUtils.TextValue(cell.getContents()) ;
            }
            int row = fisrtline+1;

            // 获取指定单元格的对象引用
            for (; row < rowSum; row++) {
            	boolean isline = isLine(readsheet,row);
            	if(!isline)
            		continue;
            	
                Record record = new Record();
                for (int i = 0; i < colSum; i++) {
                    Cell cell = readsheet.getCell(i, row);
                    String s = cell.getContents();
                    // 空值不参与导入持久化
                    if(xx.isEmpty(s))
                    	continue;
                    
                    String key = null;
                    if(enColumn != null){
                    	key = enColumn.get(i);
                    }
                    
                    if(key == null){
                    	// 根据表头CN获取EN
                    	key = field.get(headers[i]);
                    }
                
                	if(!xx.isEmpty(key)){
                		record.set(key, s);
                	}
                    
                }
                record.set(SHEET_ROWNO, row);//记录用，时候追溯数据
                list.add(record);
            }
            
            //可能需要后续处理
            ctrl.setAttr(IMP_EXCEL_SHEET, readsheet);
            ctrl.setAttr(IMP_EXCEL_SHEET_HEAD, headers);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workbook.close();
        }

        return list;
    }
    
    /**
     * 提取第一行
     * 判断标准：前3行都有数据，否则就认为是第0行是第一行
     * @param readsheet
     * @return
     */
    private static int getFistLine(Sheet readsheet){
    	int fistLine = 0;
    	for(int l=0;l<=readsheet.getRows();l++){
    		
    		boolean isFirst = true;
    		
    		isFirst = isLine(readsheet,l);
    		
    	    if(isFirst){
    	    	fistLine = l;
    	    	break;
    	    }
    	    
    	}
    	return fistLine;
    }
    
    private static boolean isLine(Sheet readsheet,int line){
    	boolean isLine = true;
    	
    	int check = checkColumn;
		if(readsheet.getColumns()<check)
			check = readsheet.getColumns();
		for(int r=0;r<=check;r++){
			Cell cell = readsheet.getCell(r, line);
			
			String content = cell.getContents();
			if(xx.isEmpty(content)){
				isLine = false;
				break;
			}
		}
		return isLine;
    }
    
    
    private static Map<String, String> getKeyValue(List<MetaField> items) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (MetaField item : items) {
            map.put(item.getCn(), item.getEn());
        }
        return map;
    }

    public static void writeExcel(InputStream is, OutputStream os, HashMap<String, String> data) throws Exception {


        WritableWorkbook wwb = null;
        WorkbookSettings settings = new WorkbookSettings();
        settings.setEncoding("UTF-8"); // 关键代码，解决中文乱码:GB18030，UTF-8，ISO-8859-1 等
        settings.setWriteAccess("eovaxls");
        // 读取模版
        Workbook in = Workbook.getWorkbook(is, settings);

        // 创建写表格
        wwb = Workbook.createWorkbook(os, in);
        // 取表格第一页
        WritableSheet sheet = wwb.getSheet(1);

        // 总列数
        int colSum = sheet.getColumns();
        // 总行数
        int rowSum = sheet.getRows();
        System.out.println(colSum);
        System.out.println(rowSum);

        sheet.addCell(new Label(1, 1, "6666666"));

        Cell cell = sheet.getCell(1, 1);
        System.out.println(cell.getContents());
        
        // 把创建的内容写入到输出流中，并关闭输出流
        wwb.write();
        wwb.close();
    }
    
    /**
     * 根据行号以及数据名（列名）提取数据
     * @param readsheet
     * @param row
     * @param header
     * @param name
     * @return
     */
    public static String getSheetRowNameValue(Sheet readsheet,int row,String[] header,String name) {
    	
    	
    	int cellNo = -1;
    	for(int i = 0;i<header.length;i++) {
    		if(name.equalsIgnoreCase(header[i].trim())) {
    			cellNo = i;
    			break;
    		}		
    	}
    	
    	if(cellNo != -1) {
    		return readsheet.getCell(cellNo, row).getContents();
    	}
    	
    	
    	return null;
    }
}