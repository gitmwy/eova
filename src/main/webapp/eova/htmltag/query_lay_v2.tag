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
	
	var diyHeight = 0;
	// 自动计算高度
	var count = 0;
	// 获取分组元字段数据
	var fields = query("fields", objectCode); 
	for(f in fields){
		// 只输出允许查询的条件,count累计一共有多少个查询条件
		if(!isTrue(f.is_query)){
			continue;
		}
		count++;
		
		// 特殊处理，因为复选框可能独占一行(本来无一物何处惹尘埃)，如果不是跟钱过不去，还是用推荐用下拉框
		if(f.type == "复选框"){
			diyHeight = diyHeight + 25;
		}
		//参照不知道为啥效果有些不一致，比如行间隔 http://lovetime.gitee.io/weadmin/pages/order/list.html
		
	%>	
					<%if(f.type == "文本框" || f.type == "文本域" || f.type == "编辑框" || f.type == "图片框" || f.type == "文件框" || f.type == "密码框"){%>
		       			 <div class="layui-inline"><#text_lay  id="${f.en}" name="query_${f.en}" placeholder="请输入${f.cn}" /></div>
					<%}else if(f.type == "日期框" || f.type == "时间框"){%>
						 <div class="layui-inline"><#times_lay id="${f.en}" name="${f.en}" start_placeholder="${f.cn}开始时间" end_placeholder="${f.cn}结束时间"  /></div>
					<%}else if(f.type == "数字框"){%>
						 <div class="layui-inline"><#num_lay id="${f.en}" name="${f.en}" start_placeholder="${f.cn}大与等于" end_placeholder="${f.cn}小于" /></div>
					<%}else if(f.type == "布尔框"){%>
						 <div class="layui-inline" title="${f.cn}"><#bool_lay item="${f}" id="${f.en}" name="query_${f.en}" /></div>	 
					
					<%}else if(f.type == "单选框"  ){%>
						 <div class="layui-inline" title="${f.cn}"><#combo_lay id="${f.en}" name="query_${f.en}"  code="${f.object_code}" field="${f.en}"  placeholder="${f.cn}"   />
						</div>	 
					<%}else{%>
						 <div class="layui-inline" ><#field_lay item="${f}" isQuery="true" placeholder="${f.cn}"  /></div>
					<%}%>
					
	<% } %>				
					
				</form>

			</div>
		</div>
		
	</div>