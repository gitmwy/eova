<div class="layui-collapse" lay-filter="quickQuery">
		<div class="layui-colla-item">
			<h2 class="layui-colla-title">查询</h2>
			
			<% if(isTrue(isShow!true) ){//没要求就是显示，有后面参数确定是否显示 %>
				<% if( object.is_show_condition!0 == 1){ %>
				<div class="layui-colla-content  layui-show">
				<% }else{ %>
				<div class="layui-colla-content">
				<% } %>
			<% }else{ %>
			<div class="layui-colla-content">
				
			<% } %>
			
				<form id="${id}" objectCode="${objectCode}" class="layui-form" >
					
				<%
				//2中模式，第一无配置信息的情况 ，第二有配置数据的情况，（如果没有配置信息则走第一种默认情况）
				//object.config配置信息如下，query格式如下，如果存在此配置，如果不存在则走默认模式（未配置的直接分成最后一组直铺）
				/* {
					"query": [{
							"line": "1",
							"data": "name,tag1,tag2"
						},
						{
							"line": "2",
							"data": "password,relation_pro"
						}
					]
				} */			
		var config = null;//from 的配置信息，比如一行几个 都有谁，如果不存在则使用目前老的模式，分组，顺序铺
		var queryView = null;


		
		if(!isEmpty(object.config!)){
			//formView = object.config[formtype];
			config = object.config.config;
			queryView = config.query!;
			
		}			
				
	var diyHeight = 0;
	// 自动计算高度
	var count = 0;
	// 获取分组元字段数据
	var fields = query("fields", objectCode); 
	
	for(l in queryView){
		//debug(l.data.getAsString!);
		var linedatas = strutil.split(l.data!, ',');
		
		%>
		
		 <div class=" layui-row layui-col-space3">
		
		<%
		var tt = 12;
		var length = linedatas.~size;//计算份额
		for(oneKey in linedatas){
			//需要判断分成几块(2种情况，)
			var remainder =  tt%length;
			
			
			//需要判断分成几块(2种情况，1种 'avatar(2),file(2)',1种 'avatar,file')
			var col = (tt-remainder)/length;
			if( strutil.index(oneKey,'(') != -1 ){
				col = strutil.subStringTo(oneKey,strutil.index(oneKey,'(')+1,strutil.index(oneKey,')'));
				oneKey = strutil.subStringTo(oneKey,0,strutil.index(oneKey,'('));
			}
			var f = null;
			for(tf in fields){
				// 只输出允许查询的条件,count累计一共有多少个查询条件
				if(!isTrue(tf.is_query)){
					continue;
				}
				
				if(tf.en == oneKey){//输出
					f = tf;
					break;	
				}	
			}
			// 特殊处理，因为复选框可能独占一行(本来无一物何处惹尘埃)，如果不是跟钱过不去，还是用推荐用下拉框
			//参照不知道为啥效果有些不一致，比如行间隔 http://lovetime.gitee.io/weadmin/pages/order/list.html
			if(f == null) 
				continue;
	%>	
					
				 <div class="layui-col-md${col}">
              <%if(f.type == "文本框" || f.type == "文本域" || f.type == "编辑框" || f.type == "图片框" || f.type == "文件框" || f.type == "密码框"){%>
		       			<#text_lay  id="${f.en}" name="query_${f.en}" placeholder="请输入${f.cn}" />
					<%}else if(f.type == "日期框" || f.type == "时间框"){%>
						 <#times_lay id="${f.en}" name="${f.en}" start_placeholder="${f.cn}开始时间" end_placeholder="${f.cn}结束时间"  />
					<%}else if(f.type == "数字框"){%>
						 <#num_lay id="${f.en}" name="${f.en}" start_placeholder="${f.cn}大与等于" end_placeholder="${f.cn}小于" />
					<%}else if(f.type == "布尔框"){%>
						 <#bool_lay item="${f}" id="${f.en}" name="query_${f.en}" /> 
					<%}else if(f.type == "单选框"  ){%>
						 <#combo_lay id="${f.en}" name="query_${f.en}"  code="${f.object_code}" field="${f.en}"  placeholder="${f.cn}"   />
					<%}else{%>
						 <#field_lay item="${f}" isQuery="true" placeholder="${f.cn}"  />
					<%}%>
            </div>
					
					
	<% } %>	
		</div>
	<% } %>				
					
				</form>

			</div>
		</div>
		
	</div>