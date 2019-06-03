/**
 * 
 */
package com.oss.user;

import com.eova.aop.AopContext;
import com.eova.aop.MetaObjectIntercept;
import com.eova.common.Easy;
import com.eova.common.utils.EncryptUtil;
import com.eova.common.utils.xx;
import com.eova.config.EovaConst;
import com.eova.model.User;
import com.eova.template.single.SingleIntercept;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * @author jin
 *
 */
public class UserInfoIntercept extends MetaObjectIntercept {
	
	

	public String addBefore(AopContext ac) throws Exception {
		super.addBefore(ac);
		
		//登录名不能重复
		String login_id = ac.record.getStr("login_id");
		
		Record user = Db.use(EovaConst.DS_MAIN).findFirst("select * from users where login_id=?", login_id);
		if(user != null)
			return "用户名重复";
		
		//存第一角色
		ac.record.set("rid", ac.record.getStr("rids").split(",")[0]);
		
		return null;
	}
	  /* 
	   * 添加从账户,以及设置默认密码
	   * (non-Javadoc)
	 * @see com.eova.aop.MetaObjectIntercept#addAfter(com.eova.aop.AopContext)
	 */
	public String addAfter(AopContext ac) throws Exception {
		
		String psw = xx.getConfig("user_df_psw", "000000");
		
		User lUser = new User();
		lUser.set("login_id", ac.record.get("login_id"));
		lUser.set("login_pwd", EncryptUtil.getSM32(psw));
		lUser.set("rid", ac.record.get("rid"));
		lUser.set("id", ac.record.get("id"));
		
		lUser.save();
			
	    return null;
	  }
	
	/* 
	 * 操作成功的提示信息
	 * (non-Javadoc)
	 * @see com.eova.aop.MetaObjectIntercept#addSucceed(com.eova.aop.AopContext)
	 */
	public String addSucceed(AopContext ac) throws Exception {
        return "添加用户成功，默认密码:"+xx.getConfig("user_df_psw", "000000");
    }
	
	public String updateBefore(AopContext ac) throws Exception {
		if(!xx.isEmpty(ac.record.getStr("rids")))
			ac.record.set("rid", ac.record.getStr("rids").split(",")[0]);
		return super.updateBefore(ac);
	}
	
	 /* 
	  * 主要是更新 角色 和 登录名
	  * (non-Javadoc)
	 * @see com.eova.aop.MetaObjectIntercept#updateAfter(com.eova.aop.AopContext)
	 */
	public String updateAfter(AopContext ac) throws Exception {
		User lUser = User.dao.findById(ac.record.get("id"));
		
		if(lUser != null){
			lUser.set("rid", ac.record.get("rid"));
			lUser.set("login_id", ac.record.get("login_id"));
			lUser.update();
		}
		
	        return null;
	}
	
	//删除暂时不处理
}
