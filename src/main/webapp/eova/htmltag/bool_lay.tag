<%
var disabled = "";
if( isTrue(isReadonly!) ||isTrue(view!)){
	disabled =   "readonly "  ;
}
var lay_text = "是|否";
if(!isEmpty(options!)){
	lay_text =   options  ;
}

var checked = "";

//新增的情况
if(isEmpty(value!)){
	checked =   "checked"  ;
}else{
	if(isTrue(value!) ){
		checked =   "checked"  ;
	} 
}
var value = "off";
if(checked == "checked"){
	value = "on";
}

%>
<div class="layui-unselect layui-form-select downpanel checkbox">
<input type="checkbox"  id="${id!}_checkbox" name="${name!}_checkbox" lay-skin="switch" lay-filter="${id!}_checkbox" lay-text="${lay_text}" ${disabled} ${checked}>
<input type="hidden" id="${id!}" name="${name!}" value="${value!}">
</div>
<script>
layui.use(['form'], function(){
	var form = layui.form;
	 //监听指定开关
	  form.on('switch(${id!}_checkbox)', function(data){
		  if(this.checked){
			  $("#${id!}").val("on");
		  }else{
			  $("#${id!}").val("off");
		  }
	    });
});

</script>
