<%
// data-options
var verify = "";
if(isTrue(isNoN!) ){
	verify = verify + "required";
}
if(!isEmpty(validator!)){
	if(isEmpty(verify))
		verify = validator;
	else
		verify = verify + "|" +validator;
}
var disabled="";
if(isTrue(isReadonly!) ){
	disabled = 'readonly';
	verify = '';
}
//后面是否会搞成 图标形式再说，以及支持外部图标
%>
<input type="text" id="${id!}" name="${name!}" value="${value!}" placeholder="${placeholder!}" ${disabled!} lay-verify="${verify!}" autocomplete="off" class="layui-input">
<script type="text/javascript">
	//点击事件,打开选中图标
	 $("#${id!}").click(function(){
		 var url = '${STATIC!}/toIcon';
		 var dex = layer.open({
			  type: 2,
			  shade: 0.3,
			  anim: 5,
			  title : "请选择图标",
			  area: ['100%', '100%'],
			  scrollbar: false,
			  content: [url, 'yes'],
			  btn: ['确定'],
		 	  yes: function(index, layero){//点击确定按钮
		   		 //按钮【按钮一】的回调
		    	console.log('确定');
		   		 
		    	var iframeWin = window[layero.find('iframe')[0]['name']];
		    	console.log(iframeWin.icon);
		    	console.log(iframeWin.iconOld);
		    	
		    	//暂时先用老的值吧（如果为空则不生效）
		    	if(iframeWin.iconOld != ''){
		    		$("#${id!}").val(iframeWin.iconOld);
		    	}
		    	layer.close(index); //如果设定了yes回调，需进行手工关闭
		 	 },
			  success: function(layero, index){//加载成功
			  }
			});  
	  });
 	    
</script>