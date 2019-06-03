package com.oss.job;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.JobExecutionContext;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eova.common.utils.xx;
import com.eova.common.utils.util.HttpClientUtil;
import com.eova.model.EovaLog;

/**
 * 每分钟执行
 *
 * @author Jieven
 * @date 2014-7-7
 */
public class EveryMinJob extends AbsJob {

	@Override
	protected void process(JobExecutionContext context) {
		System.out.println("每分钟任务");
		
		initLogs();
	}
	
	
	public static Map<String,String> cache = new ConcurrentHashMap();
	private final String query = "http://ip.taobao.com/service/getIpInfo.php?ip=";

	private void initLogs() {
		// 查询500条无城市日志，并修复
		List<EovaLog> logs = EovaLog.dao.getUnInitLogs();
		int x= 0 ;
		for (EovaLog one : logs) {
			String ip = one.getStr("ip");
			System.out.println((x++)+":"+ip);
			if (!StringUtils.isEmpty(ip)) {
				if("0:0:0:0:0:0:0:1".equalsIgnoreCase(ip) || "127.0.0.1".equalsIgnoreCase(ip)){
					one.set("city", "本地").update();

						continue;
				}
				
				String json = cache.get(ip);
				boolean isNew = false;
				if (json == null) {
					json = HttpClientUtil.getInstance().getWithRealHeader(query + ip);
					isNew = true;
					

				}
				
				
				
				
				
				if( !xx.isEmpty(json) ){
					
					{
						JSONObject jsonObj  = null;
						jsonObj = JSON.parseObject(json, JSONObject.class);
					
						Integer code = jsonObj.getInteger("code");
						if (code != null && code == 0) {
							
							if(isNew)
								cache.put(ip, json);
							
							String value = jsonObj.getJSONObject("data").getString("region") + "-"
									+ jsonObj.getJSONObject("data").getString("city");
							one.set("city", value).update();
						}
					}
					
					
				}

			}

		}

	}
			 
				
						
	
}
