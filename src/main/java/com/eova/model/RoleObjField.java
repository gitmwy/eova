/**
 * 
 */
package com.eova.model;

import java.util.List;

import com.eova.common.base.BaseModel;
import com.eova.common.utils.xx;
import com.eova.common.utils.db.DbUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * 角色显示字段(如无则无配置，不控制)
 * @author jin
 *
 */
public class RoleObjField extends BaseModel<RoleObjField> {
	private static final long serialVersionUID = -1794335434198017392L;

	public static final RoleObjField dao = new RoleObjField();
	
	public List<RoleObjField> queryByRid(int rid,String objCode) {
		return this.find("SELECT f.* FROM bb_role_obj_field f,eova_object o WHERE f.obj_id=o.id AND f.rid=? AND o.code=?",rid,objCode);
	}
	

	
	public List<RoleObjField> queryByRids(List<String> rids,String objCode) {
		return this.find("SELECT f.* FROM bb_role_obj_field f,eova_object o WHERE f.obj_id=o.id AND o.code=? and rid in"+DbUtil.joinIds(rids),objCode);
	}
	
	public List<Integer> queryIdByRids(List<String> rids,String objCode) {
		return Db.use(xx.DS_EOVA)
				.query("SELECT distinct f.field_id FROM bb_role_obj_field f,eova_object o WHERE f.obj_id=o.id AND o.code=? and f.rid in"+DbUtil.joinIds(rids)+" and f.is_show=1",objCode);
	}
	
	
	
	/**
	 * 全部
	 * @param rid
	 * @param objCode
	 * @return
	 */
	public List<Record> queryByRidAndCode(int rid,String objCode) {
		return Db.use(xx.DS_EOVA).find("SELECT f.*,rf.rid,rf.id rf_id,rf.is_show rf_is_show FROM bb_role_obj_field rf RIGHT JOIN eova_field f ON rf.field_id=f.id AND  rf.rid=?  WHERE f.object_code=?",rid,objCode);
	}
}
