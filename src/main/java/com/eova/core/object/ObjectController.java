/**
 * 
 */
package com.eova.core.object;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.eova.common.Easy;
import com.eova.common.Response;
import com.eova.common.utils.xx;
import com.eova.config.EovaConst;
import com.eova.config.PageConst;
import com.eova.model.Button;
import com.eova.model.Menu;
import com.eova.model.MetaField;
import com.eova.model.MetaObject;
import com.eova.model.Role;
import com.eova.model.RoleBtn;
import com.eova.model.RoleObjField;
import com.eova.model.User;
import com.eova.service.sm;
import com.eova.widget.WidgetUtil;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * 对象数据权限
 * @author jin
 *
 */
public class ObjectController extends Controller {
	
	/**
	 * 对象角色数据权限
	 */
	public void toObjectRoleChoose() {
		

		User user = getSessionAttr(EovaConst.USER);
		// 当前用户的下级角色
		List<Role> roles = Role.dao.findSubRole(user.getRole().getInt("lv"));
		setAttr("roles", roles);
		
		String template = this.getPara(PageConst.Template);//模板
		
		MetaObject object = MetaObject.dao.findById(getPara(0));
		
		//查询元对象的字段
		//List<MetaField> fields = MetaField.dao.queryByObjectCode(object.getCode());
		setAttr("object", object);
		//setAttr("fields", fields);
		Map roleFields = new HashMap();
		for(Role role:roles){
			List<Record> list = RoleObjField.dao.queryByRidAndCode(role.getInt("id"), object.getCode());
		
			roleFields.put(role.get("id").toString(), list);
		}
		
		setAttr("roleFields", roleFields);
		if (!xx.isEmpty(template)) 
			render("/eova/object/objectFieldAuth_"+template+".html");
		else
			render("/eova/object/objectFieldAuth.html");
	}
	
	public void saveObjectFieldRole() {
		User user = getSessionAttr(EovaConst.USER);
		int rid = this.getParaToInt("rid");
		int obj_id = this.getParaToInt("obj_id");
		String fieldId =  this.getPara("fieldId");
		// 当前用户的下级角色
		List<Role> roles = Role.dao.findSubRole(user.getRole().getInt("lv"));
		
		boolean isOwner = false;
		for(Role oner:roles){
			if(oner.getInt("id") == rid){
				isOwner = true;
				break;
			}	
		}
		
		if(!user.isAdmin() && !isOwner){
						renderJson(Response.err("非法操作."));
						return;
		}
		
		if (xx.isEmpty(fieldId)) {
			renderJson(Response.err("显示列为空"));
			return;
		}
		
		
		// 删除历史
		Db.use(xx.DS_EOVA).update("delete from bb_role_obj_field where rid = ? and obj_id=?", rid,obj_id);
		
		MetaObject object = MetaObject.dao.findById(obj_id);
		String[] fieldIds = fieldId.split(",");
		
		List<Record> list = RoleObjField.dao.queryByRidAndCode(rid, object.getCode());
		
		for(Record one : list){
			int field_id = one.getInt("id");
			RoleObjField f = new RoleObjField();
			f.set("rid", rid);
			f.set("obj_id", obj_id);
			
			
			f.set("is_show", 0);
			for(int x=0;x<fieldIds.length;x++){
				if(Integer.parseInt(fieldIds[x])  == field_id){
					f.set("is_show", 1);
					break;
				}
			}
			
			
			f.set("field_id", field_id);
			f.save();
		}
	
		renderJson(Response.suc("保存成功"));
	}
//	public void getFunJson() {
//		User user = (User)this.getSession().getAttribute(EovaConst.USER);
//		// 查所有节点
//		int rootId = 0;
//
//		// 获取登录用户的角色
//		User user = getSessionAttr(EovaConst.USER);
//		//int rid = user.getRid();
//
//		// 获取所有菜单信息
//		LinkedHashMap<Integer, Menu> allMenu = (LinkedHashMap<Integer, Menu>) sm.auth.getByParentId(rootId);
//		// 根据角色获取已授权菜单Code
//		List<String> authMenuCodeList = sm.auth.queryMenuCodeByRid(user.getRids());
//
//		// 获取已授权菜单
//		LinkedHashMap<Integer, Menu> authMenu = new LinkedHashMap<Integer, Menu>();
//		for (Map.Entry<Integer, Menu> map : allMenu.entrySet()) {
//			Menu menu = map.getValue();
//			// 未授权 也不是超级管理员
//			if (!authMenuCodeList.contains(menu.getStr("code")) && !user.isAdmin()) {
//				continue;
//			}
//			authMenu.put(map.getKey(), menu);
//		}
//
//		// 获取已授权子菜单的所有上级节点(若功能有授权，需要找到上级才能显示)
//		LinkedHashMap<Integer, Menu> authParent = new LinkedHashMap<Integer, Menu>();
//		for (Map.Entry<Integer, Menu> map : authMenu.entrySet()) {
//			WidgetUtil.getParent(allMenu, authParent, map.getValue());
//		}
//
//		// 根节点不显示排除
//		authParent.remove(rootId);
//
//		// 将已授权的子菜单 放入 已授权 父菜单 Map
//		// 顺序说明：父在前，子在后,子默认又是有序的
//		authParent.putAll(authMenu);
//
//		// 获取所有按钮信息
//		List<Button> btns = Button.dao.find("select * from eova_button where is_hide = 0 order by menu_code,group_num,order_num");
//		// 获取已授权功能点
//		List<Integer> roleBtnIds = RoleBtn.dao.findByRids(user.getRids());
//
//		// 构建菜单对应功能点 eg. [玩家管理] 口查询 口新增 口修改 口删除
//		for (Map.Entry<Integer, Menu> map : authParent.entrySet()) {
//			buildBtn(map.getValue(), btns, roleBtnIds, user.getRids());
//		}
//
//		// Map 转 Tree Json
////		String json = WidgetUtil.menu2TreeJson(authParent, rootId);
//		
//		List<Record> list = WidgetUtil.menu2Json(authParent, rootId);
//
//		renderJson(new Response(Response.STATUS_SUC,null,list.size(),list));
//	}
}
