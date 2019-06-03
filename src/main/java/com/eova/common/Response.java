/**
 * 
 */
package com.eova.common;

import java.util.List;

/**
 * @author jin
 *
 */
public class Response {
	public static final int STATUS_SUC = 200;
	public static final int STATUS_ERR_500 = 500;
	
	public static final int STATUS_FAIL = 301;
	/**
	 * 处理结果（一般情况下 status+msg（数据），如果支持分页则控件需要 total和rows）
	 * 
	 * **/
	private int status = 200;
	private String msg = "操作成功";
	private Object data = null;
	
	//分页用的
	private Integer count = null;
	
	
	public Response(){
	}
	
	public Response(Object sucData){
		this.status = STATUS_SUC;
		this.data = sucData;
	}
	
	public Response(int status, String msg){
		this.status = status;
		this.msg = msg;
	}
	
	public Response(int status, String msg,int count,Object data){
		this.status = status;
		this.msg = msg;
		this.count = count;
		this.data = data;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
	
	public static Response err(String msg){
		Response r = new Response();
		r.setStatus(STATUS_FAIL);
		r.setMsg(msg);
		return r;
	}
	public static Response suc(String msg){
		Response r = new Response();
		if(msg != null)
			r.setMsg(msg);
		return r;
	}

	public static Response sucData(Object data){
		Response r = new Response();
		
			r.setData(data);
		return r;
	}
}
