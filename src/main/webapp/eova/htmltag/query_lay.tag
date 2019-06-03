<%
//Form Widget: 。
		// 下面的代码，必须非常熟悉Beetl再看如下代码。
		// 用到Beetl各种常规语法+自定义函数+Tag
		//只是查看模式view，所有字段都是只读并且按钮为“确认”
		
		//2中模式，第一无配置信息的情况 ，第二有配置数据的情况，（如果没有配置信息则走第一种默认情况）
		var config = null;//from 的配置信息，比如一行几个 都有谁，如果不存在则使用目前老的模式，分组，顺序铺
		var queryView = null;


		
		if(!isEmpty(object.config!)){
			//formView = object.config[formtype];
			config = object.config.config;
			queryView = config.query!;
			
		}	
		
		//debug(queryView);
		
		
		if(queryView == null){//无配置文件则走默认模式
		
%>
<#query_lay_v2 id="${id!}" objectCode="${objectCode}" object="${object}"  />
<% }else{ %>
<#query_lay_cfg id="${id!}" objectCode="${objectCode}" object="${object}"  />
<% } %>

