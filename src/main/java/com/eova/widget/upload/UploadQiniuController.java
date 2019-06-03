/**
 * 
 */
package com.eova.widget.upload;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eova.common.utils.xx;
import com.eova.common.utils.io.FileUtil;
import com.eova.common.utils.io.ImageUtil;
import com.eova.common.utils.util.StringUtils;
import com.eova.config.EovaConfig;
import com.eova.config.EovaConst;
import com.eova.service.ServiceManager;
import com.eova.widget.upload.UploadController.FileStatus;
import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import com.jfinal.upload.UploadFile;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

/**
 * 上传组件（7牛云）
 * @author jin
 *
 */
public class UploadQiniuController extends UploadController {
	

	
		// 编辑器上传(编辑器奇怪的返回需求,根据富文本编辑框业务确定)
		public void editor() {
			String filedir = xx.getConfig("dir_editor");
			
			
			FileStatus status = upload(filedir,1,null);
			if(status.success){
				//  String result = "{\"name\":\""+ up.getFileName() +"\", \"originalName\": \""+ up.getOriginalName() +"\", \"size\": "+ up.getSize() +", \"state\": \""+ up.getState() +"\", \"type\": \""+ up.getType() +"\", \"url\": \""+ up.getUrl() +"\"}";
				Map result = new HashMap();
				result.put("state", "SUCCESS");
				result.put("url",  status.fileName);
				result.put("name", status.getSrcName());
				result.put("originalName", status.getSrcName());
				
				
				this.renderJson(result);
				
			}else{
				renderText(status.msg);
			}
		}
	
		
		protected FileStatus upload( String fileDir,int type,String name) {
			FileStatus status = super.upload(fileDir, type, name);
			
			
			if(status.success){//处理成功的云存储
				
			
				
				//执行完成，修改status transStatus
				ServiceManager.qiniuService.upload(status);
			}
			
			return status;
		}
		
		
		
}
