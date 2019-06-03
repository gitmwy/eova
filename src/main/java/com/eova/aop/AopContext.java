/**
 * Copyright (c) 2013-2016, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.eova.aop;

import java.util.ArrayList;
import java.util.List;

import com.eova.config.EovaConst;
import com.eova.model.MetaObject;
import com.eova.model.User;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;


/**
 * AOP 上下文
 *
 * @author Jieven
 * @date 2014-8-29
 */
public class AopContext {

    /**
     * 当前控制器
     */
    public Controller ctrl;

    /**
     * 当前用户对象
     */
    public User user;

    /**
     * 当前操作数据集(批量操作)
     */
    public List<Record> records;

    /**
     * 当前操作对象(单条数据操作)
     */
    public Record record;

    /**
     * 当前操作对象固定值
     * 用途：新增/编辑时预设固定初始值
     * 推荐：固定初始值，建议禁用字段使用addBefore()拦截添加值
     */
    public Record fixed;

    /**
     * 当前操作元对象
     */
    public MetaObject object;

    /**
     * 追加SQL条件
     */
    public String condition = " ";
    /**
     * 自定义SQL覆盖默认查询条件
     * 格式: where xxx = xxx
     */
    public String where;
    /**
     * 自定义SQL参数
     */
    public List<Object> params = new ArrayList<Object>();
    /**
     * 自定义SQL覆盖默认排序
     * 格式: order by xxx desc
     */
    public String sort;

    public AopContext(Controller ctrl){
        this.ctrl = ctrl;
        this.user = (User)ctrl.getSessionAttr(EovaConst.USER);
    }

//    public AopContext(Controller ctrl, List<Record> records){
//        this(ctrl);
//        this.records = records;
//    }
//    public AopContext(Controller ctrl, Record record){
//        this(ctrl);
//        this.record = record;
//    }
    
    public AopContext(Controller ctrl, List<Record> records,MetaObject object){
        this(ctrl);
        this.records = records;
        this.object = object;
    }

    public AopContext(Controller ctrl, Record record,MetaObject object){
        this(ctrl);
        this.record = record;
        this.object = object;
    }

}