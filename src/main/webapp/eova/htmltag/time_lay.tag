<%
//debug(f);
//debug(value);
// data-options
var verify = "";
if(isTrue(isNoN!) ){//时间字段只有要不要校验
	verify = verify + "required";
}


if(!isEmpty(value!)){
	
	if(!strutil.contain(value!+'','$')){
		value = strutil.formatDate(value!, options!);
	}
}
if(!isTrue(isReadonly!) && !isTrue(view!)){
	verify = '';
}
//options =yyyy-MM-dd HH:mm:ss 要么  yyyy-MM-dd
%>
<input type="text" class="layui-input" lay-verify="${verify!}" id="${id!}" name="${name}" readonly value="${value!}" placeholder="${options}">
<script>
layui.use('laydate', function(){
  var laydate = layui.laydate;
  
  <% if(!isTrue(isReadonly!) && !isTrue(view!)){//只读模式不会触发时间选择  %>
	  //日期时间选择器
	  laydate.render({
	    elem: '#${id!}'
	    	<% if(options == "yyyy-MM-dd HH:mm:ss"){ %>
	    		,type: 'datetime'
	    	<% }else{ %>
	    		,type: 'date'
	    	<% } %>
	
	    		  ,isInitValue: true //允许填充初始值
	    		  ,calendar: true
	  });
 <% } %>
});
</script>