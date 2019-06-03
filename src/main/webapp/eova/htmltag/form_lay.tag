<%
//Form Widget: 。
		// 下面的代码，必须非常熟悉Beetl再看如下代码。
		// 用到Beetl各种常规语法+自定义函数+Tag
		//只是查看模式view，所有字段都是只读并且按钮为“确认”
		
		//2中模式，第一无配置信息的情况 ，第二有配置数据的情况，（如果没有配置信息则走第一种默认情况）
		var config = null;//from 的配置信息，比如一行几个 都有谁，如果不存在则使用目前老的模式，分组，顺序铺
		var formView = null;
		
		var formtype = type+'Form';
		if(!isEmpty(VIEW_FORM!))
			formtype = VIEW_FORM!;
		
		
		debug(VIEW_FORM!);
		if(!isEmpty(object.config!)){
			//formView = object.config[formtype];
			config = object.config.config;
			if(!isEmpty(VIEW_FORM!)){
				
				formView = config[VIEW_FORM];
			}else{
				formView = config[formtype];
				if(formView == null){
					formView = config.form;
				}
			}
		}
		
		var force = 0;
		if(!isEmpty(formView.force!)){
			force = formView.force!;
		}
		//0-1-2
		//目前json配置：0、1是普通模式，2是由拖拽形成的，多了一些额外参数，目前2情况
		
		

if(formView == null){//无配置文件则走默认模式	
%>
<#form_lay_default id="${id}" type="${formtype}" objectCode="${objectCode}" fixed="${fixed!}" object="${object}" data="${data!}"  view="${view!}"/>
<% }else if(force == 2){ //以后推荐拖拽形成的json ，当然也可以手工形成%>

<#form_lay_cfg_V2 id="${id}" type="${formtype}" objectCode="${objectCode}" fixed="${fixed!}" object="${object}" data="${data!}"  view="${view!}"/>

<% }else{ %>
<#form_lay_cfg id="${id}" type="${formtype}" objectCode="${objectCode}" fixed="${fixed!}" object="${object}" data="${data!}"  view="${view!}"/>
<% } %>

