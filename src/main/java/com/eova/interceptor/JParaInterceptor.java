package com.eova.interceptor;

import java.util.Date;
import java.util.UUID;

import com.alibaba.druid.util.StringUtils;
import com.eova.common.utils.xx;
import com.eova.common.utils.time.DateUtil;
import com.eova.common.utils.util.AntPathMatcher;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.service.sm;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Const;
import com.jfinal.core.Controller;
import com.jfinal.core.JFinal;

/**
 * 注意将此拦截器设置为JFinal拦截器列表中的第一个，否则上游拦截器获取URL参数时可能会有问题
 * 还有mssql 一些参数控制，如ID（uniqueidentifier），
 * 
 * @author 多多
 *
 */
public class JParaInterceptor implements Interceptor {

	private static final String EOVA = Const.DEFAULT_URL_PARA_SEPARATOR;
	private static final String MY = "&";// 自定义分隔符

	public JParaInterceptor() {
		JFinal.me().getConstants().setUrlParaSeparator(MY);
		System.err.println("set JFinal urlParaSeparator:" + MY);
	}

	@Override
	public void intercept(Invocation inv) {

		Controller target = inv.getController();
		String urlPara = target.getPara();

		// 如果URL参数不包含EOVA使用的分隔符，则不需要转义
		if (xx.isEmpty(urlPara) || !urlPara.contains(EOVA) || MY.equals(EOVA)) {
			inv.invoke();
			return;
		}

		// "/form/update"
		// "/form/detail"
		// 以上两个请求只转义第一个分隔符，保留后续字符（即更新、查看的主键值）
		// 除以上特殊请求，其他请求全部转义
		AntPathMatcher pm = new AntPathMatcher();
		String uri = inv.getActionKey();
		if (pm.match("/form/update", uri)) {
			urlPara = urlPara.replaceFirst(EOVA, MY);
		} else if (pm.match("/form/detail", uri)) {
			urlPara = urlPara.replaceFirst(EOVA, MY);
		} else if (pm.match("/widget/findCnByEn", uri)) {
			// 转义前两个分隔符
			for (int i = 0; i < 2; i++) {
				urlPara = urlPara.replaceFirst(EOVA, MY);
			}
		} else {
			urlPara = urlPara.replace(EOVA, MY);
		}
		
		//insert 的主键字段，满足类型（）或者 varchar长度是36，且为空，则主动给他添加一个
		//createtime类似字段需要添加时间
		///form/doAdd
//		if (pm.match("/form/doAdd", uri)) {
//			
//			String objectCode = inv.getController().getPara(0);
//			
//			if(objectCode == null){
//				target.setUrlPara(urlPara);
//				inv.invoke();
//				return;
//			}
//			
//			
//			final MetaObject object = sm.meta.getMeta(objectCode);
//			if(object == null){
//				target.setUrlPara(urlPara);
//				inv.invoke();
//				return;
//			}
//			
//			String pk = object.getPk();// 控件类型
//			// 获取字段当前的值
//			for (MetaField item : object.getFields()) {
//				// System.out.println(item.getEn() +'='+ c.getPara(item.getEn()));
//				String type = item.getStr("type");// 控件类型
//				String key = item.getEn();// 字段名
//				String data_type_name =(String) item.get("data_type_name");
//				int dataSize = item.getDataSize();
//				//data_size
//
//				// 获当前字段 Requset Parameter Value，禁用=null,不填=""
//				String value = inv.getController().getPara(key);
//				
//				
//				//mssql专用，uniqueidentifier 类型数据前段无视，本地补充
//				if(key.equalsIgnoreCase(pk)){
//					
//					if(!StringUtils.isEmpty(value) ){
//						String newValue = null;
//						if("uniqueidentifier".equalsIgnoreCase(data_type_name)){
//							newValue = UUID.randomUUID().toString();
//						}else if("varchar".equalsIgnoreCase(data_type_name) && dataSize == 36){
//							newValue = UUID.randomUUID().toString();
//						}
//						
//						if(value == null){//添加
//							urlPara += MY+key+"="+newValue;
//						}else{//修改
//							urlPara += urlPara.replace(key+"=", key+"="+newValue);//好像有问题吧
//						}
//					}
//					//break;
//				}else if(key.toLowerCase().indexOf("create")!=-1 && "DATETIME".equalsIgnoreCase(data_type_name)){//createtime
//					if(!StringUtils.isEmpty(value) ){
//						if(value == null){//添加
//							urlPara += MY+key+"="+DateUtil.format(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS);
//						}else{//修改
//							urlPara += urlPara.replace(key+"=", key+"="+DateUtil.format(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS));//好像有问题吧
//						}
//					}
//				}
//					
//					
//
//			}
//
//			System.out.println("set JFinal after urlPara:" + urlPara); 
//		}
		
		// 更新URL参数
		target.setUrlPara(urlPara);
		inv.invoke();

	}

}
