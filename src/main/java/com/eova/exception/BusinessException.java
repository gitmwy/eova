package com.eova.exception;


/**
 * @author JZHAO
 *
 */
public class BusinessException extends Exception  {
	  /**
     * code     返回码
     * msg      返回码描述
     * subCode  详细返回码
     * subMsg   详细返回码描述
     */
    protected int code; //业务通用错误
    protected String msg;
    protected String subCode;
    protected String subMsg;

	public BusinessException(int code,String msg) {
		
		super(msg);
		 this.code = code;
	     this.msg = msg;
	}

	public BusinessException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getSubCode() {
		return subCode;
	}

	public void setSubCode(String subCode) {
		this.subCode = subCode;
	}

	public String getSubMsg() {
		return subMsg;
	}

	public void setSubMsg(String subMsg) {
		this.subMsg = subMsg;
	}
	
	
}
