/**
 * 
 */
package com.oss.user;

import com.eova.common.Response;
import com.eova.config.EovaConst;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.oss.model.Product;

/**
 * 用户管理
 * @author jin
 *
 */
public class UserInfoController extends Controller {
	
	/**
	 * 远程校验用户是否存在
	 * @throws Exception
	 */
	public void isUserExit() throws Exception {
		String login_id = this.getPara("login_id");
		
		String id = this.getPara("id");
		
		Record user = Db.use(EovaConst.DS_MAIN).findFirst("select * from t_user where login_id=?", login_id);
		
		if(user == null || user.get("id").toString().equalsIgnoreCase(id))
			renderJson(Response.suc(""));
		else
			renderJson(Response.err("用户存在."));
		
	}

}
