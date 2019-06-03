<%// 默认返回false防止浏览器自动提交表单%>
<form id="${id}" class="layui-form" >
<div class="layui-fluid">
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
<div class="layui-row">

<!--       	<div class="layui-row "> -->
      		 <div class="layui-col-xs3" ><p>${f.cn}:</p></div>
      		 <div class="layui-col-xs8">
			<%if(f.type == "文本框" || f.type == "文本域" || f.type == "编辑框" || f.type == "图片框" || f.type == "文件框"){%>
       			 <#text_lay id="${f.en}" name="query_${f.en}" />
			<%}else if(f.type == "日期框" || f.type == "时间框"){%>
				 <#times_lay id="${f.en}" name="${f.en}" />
			<%}else if(f.type == "数字框"){%>
				 <#num_lay id="${f.en}" name="${f.en}" />
			<%}else{%>
				 <#field_lay item="${f}" isQuery="true" />
			<%}%></div>
<!-- 		</div> -->
		
  
</div>
<%}%>
</div>
</form>
<script>
</script>