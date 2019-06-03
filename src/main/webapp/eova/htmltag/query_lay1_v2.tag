<%// 默认返回false防止浏览器自动提交表单%>
<div class="layui-collapse" lay-filter="quickQuery">
		<div class="layui-colla-item">
			<h2 class="layui-colla-title">查询</h2>

			<div class="layui-colla-content  layui-show">
		

<form id="${id}" class="layui-form" >
<%
var count = 0;
	// 获取分组元字段数据
	for(f in items){
		// 只输出允许查询的条件,count累计一共有多少个查询条件
		if(!isTrue(f.is_query)){
			continue;
		}
		count++;
		//一行一个比较合适吧 	
	%>


<!--       	<div class="layui-row "> -->
      		<%if(f.type == "文本框" || f.type == "文本域" || f.type == "编辑框" || f.type == "图片框" || f.type == "文件框" || f.type == "密码框"){%>
		       			 <div class="layui-inline"><#text_lay  id="${f.en}" name="query_${f.en}" placeholder="请输入${f.cn}" /></div>
					<%}else if(f.type == "日期框" || f.type == "时间框"){%>
						 <div class="layui-inline"><#times_lay id="${f.en}" name="${f.en}" start_placeholder="${f.cn}开始时间" end_placeholder="${f.cn}结束时间"  /></div>
					<%}else if(f.type == "数字框"){%>
						 <div class="layui-inline"><#num_lay id="${f.en}" name="${f.en}" start_placeholder="${f.cn}大与等于" end_placeholder="${f.cn}小于" /></div>
					<%}else if(f.type == "布尔框"){%>
						 <div class="layui-inline" title="${f.cn}"><#bool_lay item="${f}" id="${f.en}" name="query_${f.en}" /></div>	 
					<%}else{%>
						 <div class="layui-inline" ><#field_lay item="${f}" isQuery="true" placeholder="${f.cn}"  /></div>
					<%}%>
<!-- 		</div> -->
		
  

<%}%>
		</form>

			</div>
		</div>
		
	</div>

<script>
</script>