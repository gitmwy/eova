/**
 * 
 */
package com.eova.service;

import com.eova.common.base.BaseService;
import com.eova.common.utils.xx;
import com.eova.common.utils.util.StringUtils;
import com.eova.widget.upload.UploadQiniuController;
import com.jfinal.plugin.activerecord.Db;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

/**
 * 7牛数据服务
 * @author jin
 *
 */
public class QiniuService extends BaseService {
	//用户新建
    Auth auth = null;
	UploadManager uploadManager = null;
	String defaultBucketName = null;
	
	public QiniuService(){
		//accessKey, String secretKey
//		auth=Auth.create("gkmv2ZdnnWbmKHTh0HUugMp9M_n4USkqz3_KY2xf", "Ago_-Qfc9ISI5hE04IXeSIG6amOAOkz5zA1nr6ch");
		auth=Auth.create(xx.getConfig("qiniu_access_key"), xx.getConfig("qiniu_secret_key"));
		//根据存放的机房 选择对象   这里自动选择
	    Configuration cfg=new Configuration(Zone.autoZone());
		uploadManager = new UploadManager(cfg);
		
		this.defaultBucketName = xx.getConfig("qiniu_bucket_name");
	}
	
	/**
	 * 尽量上传至7牛（入库即使失败可以后台服务继续处理）
	 * @param status
	 */
	public void upload(UploadQiniuController.FileStatus status){
		String key = StringUtils.getUUID32();//目前调整为直接用uuid吧，因为文件名其他地方另存了
		
		if(status.getSrcName().indexOf(".") != -1){
//			key = status.getSrcName().substring(0,status.getSrcName().lastIndexOf(".")-1) +"_"+ System.currentTimeMillis() //是不是可以搞短点，以及检测文件名是否非法（非法字符）
//					+status.getSrcName().substring(status.getSrcName().lastIndexOf("."));
			key += status.getSrcName().substring(status.getSrcName().lastIndexOf("."));
		}
//		else
//			key = status.getSrcName() +"_"+ System.currentTimeMillis();
		//进一步处理，上传至7牛云，并重新组织（入库并可以后期补救）
		String domain = xx.getConfig("domain_oss_static");
		//
		boolean result = upload(status.getBaseDir()+status.getFileName(),key,null);
		//数据保存业务暂时未实现，
		//Db.use(xx.DS_EOVA).save(tableName, record);
		if(result){
//			status.setFileName(key);
			status.setFileName(domain+"/"+key);
			status.setDomain(domain);
			status.setTransStatus(true);
		}else{//失败则直接使用本地，返回
			
		}
	}
	
	
	/**
	 * 获取凭证
	 * 
	 * @param bucketName 七牛云的数据库名称
	 * @return
	 */
	public String getUpToken(String bucketName) {
		return auth.uploadToken(bucketName);
	}
 
	/**
	 * 上传(目前api使用无法返回错误详情，后期再测试)
	 * 
	 * @param filePath
	 *            文件路径 （也可以是字节数组、或者File对象）
	 * @param key
	 *            上传到七牛上的文件的名称 （同一个空间下，名称【key】是唯一的）
	 * @param bucketName
	 *            空间名称 （这里是为了获取上传凭证）
	 */
	public boolean upload(String filePath, String key, String bucketName) {
		try {
			if(bucketName == null)
				bucketName = this.defaultBucketName;
			// 调用put方法上传
			Response res = uploadManager.put(filePath, key, getUpToken(bucketName));
			// 打印返回的信息
			
			return true;
		} catch (QiniuException e) {
			e.printStackTrace();
			Response r = e.response;
			// 请求失败时打印的异常的信息
			System.out.println(r.toString());
			try {
				// 响应的文本信息
				System.out.println(r.bodyString());
			} catch (QiniuException qe) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
