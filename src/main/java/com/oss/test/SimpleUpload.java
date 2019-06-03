package com.oss.test;


import com.eova.common.utils.util.StringUtils;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
 
public class SimpleUpload {
	 //用户新建
    Auth auth=Auth.create("gkmv2ZdnnWbmKHTh0HUugMp9M_n4USkqz3_KY2xf", "Ago_-Qfc9ISI5hE04IXeSIG6amOAOkz5zA1nr6ch");
	//根据存放的机房 选择对象   这里自动选择
    Configuration cfg=new Configuration(Zone.autoZone());
	UploadManager uploadManager = new UploadManager(cfg);
	
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
	 * 上传
	 * 
	 * @param filePath
	 *            文件路径 （也可以是字节数组、或者File对象）
	 * @param key
	 *            上传到七牛上的文件的名称 （同一个空间下，名称【key】是唯一的）
	 * @param bucketName
	 *            空间名称 （这里是为了获取上传凭证）
	 */
	public void upload(String filePath, String key, String bucketName) {
		try {
			// 调用put方法上传
			Response res = uploadManager.put(filePath, key, getUpToken(bucketName));
			// 打印返回的信息
			System.out.println(res.bodyString());
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
	}
 
	/**
	 * 主函数：程序入口，测试功能
	 *
	 */
	public static void main(String[] args) {
		// 上传文件的路径，因为在Mac下，所以路径和windows下不同
		String filePath = "C:\\image2060.pdf";
		// 要上传的空间
		String bucketName = "building-block1";
		// 上传到七牛后保存的文件名
		String key = "zj03";
		key = StringUtils.getUUID32();
		new SimpleUpload().upload(filePath, key, bucketName);
		//http://pbsacitgr.bkt.clouddn.com/zj01
	
	}
}