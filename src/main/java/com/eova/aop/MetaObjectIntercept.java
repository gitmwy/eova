/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.aop;

import java.util.Date;

import com.eova.common.utils.xx;
import com.eova.config.EovaConst;
import com.eova.model.MetaField;

/**
 * <pre>
 * 元对象拦截器（默认在新建、修改时的 时间字段，）
 *
 * 背景：业务不仅仅是单表的增删改查
 * 作用：对元对象持久化进行拦截切入，从而进行业务能力拓展
 *
 * AOP如何弹窗提示？
 * return null;// 逻辑正常，没有消息就是好消息
 * return Easy.info("弹出一个提示消息");
 * return Easy.warn("弹出一个警告消息");
 * return Easy.error("弹出一个错误消息");
 * return "弹出一个默认提示";
 * throw new Exception("抛出一个业务异常！！");// 事务回滚
 * </pre>
 *
 * @author Jieven
 * @date 2014-8-29
 */
public class MetaObjectIntercept {

    /**
     * 查询前置任务(DIY复杂查询条件)
     * <pre>
     * 用法：获取查询条件值
     * ac.ctrl.getPara("query_查询条件字段名");
     *
     * 用法：追加条件
     * ac.condition = "and id < ?";
     * ac.params.add(999);
     *
     * 用法：覆盖条件
     * ac.where = "where id < ?";
     * ac.params.add(5);
     *
     * 用法：覆盖排序
     * ac.sort = "order by id desc";
     * </pre>
     */
    public void queryBefore(AopContext ac) throws Exception {
    }

    /**
     * 查询后置任务
     * <pre>
     * ac.records    获取查询数据集合
     * -------------
     * // 遍历数据集，进行数据操作
     * for (Record x : ac.records){
     *
     *    x.set("total", x.getInt("a") + x.getInt("b"));// 动态计算，total为虚拟字段
     *    x.set("price", String.format("%.2f", price)); // RMB格式化
     *
     * }
     * </pre>
     */
    public void queryAfter(AopContext ac) throws Exception {
    }

    /**
     * 新增页初始化
     * <pre>
     * ac.fixed        当前操作对象固定初始值
     * -------------
     * 用法：初始化默认值
     * ac.fixed("reply", "您好，");// 回复内容给统一前缀
     * </pre>
     */
    public void addInit(AopContext ac) throws Exception {
    }

    /**
     * 新增前置任务(事务内) ，添加默认执行方法（新增字段处理）
     * <pre>
     * ac.record 当前操作数据
     * -------------
     * 用法：自动赋值
     * ac.record.set("reg_time", new Date());
     * ac.record.set("user_id", ac.user.get("id"));
     *
     * 用法：唯一值判定
     * int count = Db.queryInt("select count(*) from users where name = ?", ac.record.getStr("name"));
     * if (count > 0) {
     *     return Easy.error("名字不能重复");
     * }
     * </pre>
     */
    public String addBefore(AopContext ac) throws Exception {
    	//原则就是 table 根据配置的create_time字段（且表存在此字段）则设置默认时间
    	String field_create_time = xx.getConfig(EovaConst.TABLE_CREATE_TIME, "create_time");
    	
    	
    	if (!xx.isEmpty(ac.object.getTable())) {
    		for(MetaField field: ac.object.getFields()) {
    			
        		if(field.getEn().equalsIgnoreCase(field_create_time)) {
        			ac.record.set(field.getEn(), new Date());
        			break;
        		}
        	}
    	}else {//视图的再议
    		
    	}
    
    	
    	
        return null;
    }

    /**
     * 新增后置任务(事务内)
     * <pre>
     * ac.record 当前操作数据
     * -------------
     * 用法：级联新增，需在同事务内完成
     * int id = ac.record.getInt("id");// 获取当前对象主键值，进行级联新增
     * </pre>
     */
    public String addAfter(AopContext ac) throws Exception {
        return null;
    }

    /**
     * 新增成功之后(事务外),返回信息为需要提示的信息
     * <pre>
     * ac.records 当前操作数据集：Form模式只有一个对象，Grid模式会有多个对象
     * -------------
     * 用法：关联操作
     * 例：重置缓存，记录日志...
     * </pre>
     */
    public String addSucceed(AopContext ac) throws Exception {
        return null;
    }

    /**
     * 删除前置任务(事务内)
     * <pre>
     * ac.record    当前删除对象数据
     * -------------
     * 用法：删除前置检查
     * </pre>
     */
    public String deleteBefore(AopContext ac) throws Exception {
        return null;
    }

    /**
     * 删除后置任务(事务内)
     * <pre>
     * ac.record    当前删除对象数据
     * -------------
     * 用法：级联删除，父对象删除时级联删除子对象，同一个事务内
     * </pre>
     */
    public String deleteAfter(AopContext ac) throws Exception {
        return null;
    }

    /**
     * 删除成功之后(事务外)<br>
     *
     * ac.record    当前删除对象数据
     *
     */
    public String deleteSucceed(AopContext ac) throws Exception {
        return null;
    }

    /**
     * 修改页初始化
     * <pre>
     * ac.record    当前操作对象数据
     * ac.fixed        当前操作对象固定初始值
     * -------------
     * 用法：初始化默认值
     * ac.fixed("update_time", new Date());// 自动填写当前时间
     * </pre>
     */
    public void updateInit(AopContext ac) throws Exception {
    }

    /**
     * 更新前置任务(事务内)
     * <pre>
     * ac.record 当前操作数据
     * -------------
     * 用法：自动赋值
     * ac.record.set("update_time", new Date());
     * ac.record.set("user_id", ac.user.get("id"));
     *
     * 用法：唯一值判定
     * int count = Db.queryInt("select count(*) from users where name = ?", ac.record.getStr("name"));
     * if (count > 0) {
     *     return Easy.error("名字被占用了");
     * }
     * </pre>
     */
    public String updateBefore(AopContext ac) throws Exception {
    	//原则就是 table 根据配置的update_time字段（且表存在此字段）则设置默认时间
    	String field_update_time = xx.getConfig(EovaConst.TABLE_UPDATE_TIME, "update_time");
    	
    	
    	if (!xx.isEmpty(ac.object.getTable())) {
    		for(MetaField field: ac.object.getFields()) {
    			
        		if(field.getEn().equalsIgnoreCase(field_update_time)) {
        			ac.record.set(field.getEn(), new Date());
        			break;
        		}
        	}
    	}else {//视图的再议
    		
    	}
    	
        return null;
    }

    /**
     * 更新后置任务(事务内)<br>
     * <pre>
     * ac.record 当前操作数据
     * -------------
     * 用法：级联修改，需在同事务内完成
     * </pre>
     */
    public String updateAfter(AopContext ac) throws Exception {
        return null;
    }

    /**
     * 更新成功之后(事务外)<br> 返回信息为提示信息
     * <pre>
     * ac.records 当前操作数据集：Form模式只有一个对象，Grid模式会有多个对象
     * -------------
     * 用法：关联操作
     * 例：重置缓存，记录日志...
     * </pre>
     */
    public String updateSucceed(AopContext ac) throws Exception {
        return null;
    }
    
    /**
     * 查看前触发
     * @param ac
     * @return
     * @throws Exception
     */
    public String detailBefore(AopContext ac) throws Exception {
    	return null;
    }
    /**
     * 查看后
     * @param ac
     * @return
     * @throws Exception
     */
    public String detailAfter(AopContext ac) throws Exception {
    	return null;
    }

}