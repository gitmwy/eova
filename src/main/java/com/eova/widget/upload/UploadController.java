/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.widget.upload;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.eova.common.utils.xx;
import com.eova.common.utils.io.FileUtil;
import com.eova.common.utils.io.ImageUtil;
import com.eova.config.EovaConfig;
import com.eova.config.EovaConst;
import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import com.jfinal.upload.UploadFile;

/**
 * 上传组件（本地）
 * 
 * @author Jieven
 * 
 */
public class UploadController extends Controller {

	// 异步传图(返回必须是html,否则无法解析)
	public void img() {
		String name = getPara("name");
		if (xx.isEmpty(name)) {
			 renderHtml("{\"success\":false, \"msg\": \"找不到上传控件，一开始是拒绝的！\"}");
			 return;
		}
		
		FileStatus status = upload(null,1,name);
		
		renderHtml("{\"success\":"+status.success+", \"msg\": \""+status.msg+"\", \"fileName\": \"" + status.fileName + "\"}");
	}

	// 异步传文件（目前和 tempfile 一致至于之前的有删除零时文件的逻辑被我删除）
	public void file() {
		String name = getPara("name");
		if (xx.isEmpty(name)) {
			 renderHtml("{\"success\":false, \"msg\": \"找不到上传控件，一开始是拒绝的！\"}");
			 return;
		}
		
		FileStatus status = upload(null,2,name);
		renderHtml("{\"success\":"+status.success+", \"msg\": \""+status.msg+"\", \"fileName\": \"" + status.fileName + "\"}");
	}

	// 异步上传临时文件（此接口目前未用）
	public void tempfile() {
		
		String name = getPara("name");
		if (xx.isEmpty(name)) {
			 renderHtml("{\"success\":false, \"msg\": \"找不到上传控件，一开始是拒绝的！\"}");
			 return;
		}
		
		FileStatus status = upload(null,2,name);
		renderHtml("{\"success\":"+status.success+", \"msg\": \""+status.msg+"\", \"fileName\": \"" + status.fileName + "\"}");
	}



	// 编辑器上传(编辑器奇怪的返回需求,根据富文本编辑框业务确定)
	public void editor() {
		
		String filedir = xx.getConfig("dir_editor");
		String tempfileDir = getPara("filedir");
		//“/upload/editor”+“/img”
		if (!xx.isEmpty(tempfileDir)) {
			filedir = filedir+tempfileDir;
		}
		FileStatus status = upload(filedir,1,null);
//		{
//		    "state": "SUCCESS",
//		    "url": "upload/demo.jpg",
//		    "title": "demo.jpg",
//		    "original": "demo.jpg"
//		}
		

		
		if(status.success){
			//  String result = "{\"name\":\""+ up.getFileName() +"\", \"originalName\": \""+ up.getOriginalName() +"\", \"size\": "+ up.getSize() +", \"state\": \""+ up.getState() +"\", \"type\": \""+ up.getType() +"\", \"url\": \""+ up.getUrl() +"\"}";
			Map result = new HashMap();
			result.put("state", "SUCCESS");
			result.put("url", status.getDomain() + "/" + status.fileName);
			result.put("name", status.getSrcName());
			result.put("originalName", status.getSrcName());
			
			
			this.renderJson(result);
			
		}else{
			renderText(status.msg);
		}	
	}

	
	
	/**
	 * 通用上传文件
	 * @param fileDir 指定基本路径
	 * @param type 类型；1-图片，2-文件
	 * @param name 收到的文件名，部分控件不支持送文件名则不用送
	 * @return
	 */
	protected FileStatus upload( String fileDir,int type,String name) {
		if(fileDir == null){//未指定路径，则从req参数提取
			fileDir = getPara("filedir");
			if (xx.isEmpty(fileDir)) {
				fileDir = xx.getConfig("eova_upload_temp");
			}
		}
		
		String fileName = "";
		boolean isDelete = true;
		UploadFile file = null;
		String srcName = null;
		String domain = null;
		try {
			if (name != null) {
				// 按参数名获取上传文件
				file = getFile(name, fileDir);
			} else {
				// 获取第一个上传文件
				List<UploadFile> files = getFiles(fileDir);
				if (xx.isEmpty(files)) {
					return new FileStatus(false,"请选择一个文件！");
				}
				file = files.get(0);
			}
			
			
			// 上传到/upload 目录（也有可能冲突）
			if (xx.isEmpty(file)) {
				return new FileStatus(false,"请选择一个文件！");
			}
			
			int limit = 5120;
			
			if(type == 1){
				limit = EovaConst.UPLOAD_IMG_SIZE;
				domain = xx.getConfig("domain_img");
			}else if(type == 2){
				limit = EovaConst.UPLOAD_FILE_SIZE;
				domain = xx.getConfig("domain_file");
			}
			
			// 大小限制
			if (FileUtil.checkFileSize(file.getFile(), limit)) {
					String msg = "图片大小不能超过"+limit/(1024)+"M";
					return new FileStatus(false,msg);
			}
			// 文件后缀
			if(type == 1)
				if (!FileUtil.checkFileType(file.getFile().getPath(), true)) {
					return new FileStatus(false,"请勿上传非法类型文件");
				}
			else if(type == 2)
				if (!FileUtil.checkFileType(file.getFile().getPath(), false)) {
					return new FileStatus(false,"请勿上传非法类型文件");
				}

			// 获取文件名(文件原名)
			srcName = file.getFileName();
			// 获取文件后缀
			String suffix = FileUtil.getFileType(srcName);
			// 创建新的随机文件名（也有可能冲突）
			fileName = System.currentTimeMillis() + suffix;

			// 新文件 Path
			String path = file.getUploadPath() + File.separator + fileName;

			FileUtil.rename(file.getFile().getPath(), path);
			System.out.println(file.getFile().getPath());
			System.out.println(path);
			
			fileName = fileDir +"/"+ fileName;
			
			isDelete = false;
			
		} catch (Exception e) {
			e.printStackTrace();
			return new FileStatus(false,"系统异常：文件上传失败,请稍后再试");
		} finally {
			if (isDelete) {
				// 强制删除已上传的文件(如果还存在)
				FileUtil.delete(file.getFile());
				LogKit.info("图片回收");
			}
		}
		
		return new FileStatus(true,"上传成功",srcName,fileName,xx.getConfig("static_root"),domain);
	}
	
	public class FileStatus {
	    boolean success = false;
	    String msg = null;
	    String srcName = null;// 文件源名称
	    String fileName = null; // 文件相对路径： /upload/aaaa.jpg 如果经过云存储则此字段数据修改成：http：//xx/aaaaas
	    String baseDir = null; //文件base路径，系统中配置 的 /opt/apache-tomcat-7.0.69/webapps/files
	    //添加的访问域名如：http://127.0.0.1
	    String domain = null;
	    boolean transStatus = false;
	    
	    public FileStatus(boolean success,String msg) {
	    	this.success = success;
	    	this.msg = msg;
	    }
	    
	    public FileStatus(boolean success,String msg,String srcName,String fileName,String baseDir,String domain) {
	        this.success = success;
	        this.msg = msg;
	        
	        this.srcName = srcName;
	        this.fileName = fileName;
	        this.baseDir = baseDir;
	        this.domain = domain;
	    }

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public String getSrcName() {
			return srcName;
		}

		public void setSrcName(String srcName) {
			this.srcName = srcName;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getBaseDir() {
			return baseDir;
		}

		public void setBaseDir(String baseDir) {
			this.baseDir = baseDir;
		}

		public String getDomain() {
			return domain;
		}

		public void setDomain(String domain) {
			this.domain = domain;
		}

		public boolean isTransStatus() {
			return transStatus;
		}

		public void setTransStatus(boolean transStatus) {
			this.transStatus = transStatus;
		}
	     
	    
	}
}