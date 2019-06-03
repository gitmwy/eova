<%
// data-options
var data = "";
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
if(isTrue(isReadonly!) ||isTrue(view!)){
	disabled = 'readonly';
	verify = '';
}
var ismultiple = true;
if(!isEmpty(multiple!)){
	ismultiple = multiple;
}

// 默认URL
var findUrl = "";
if(!isEmpty(code) && !isEmpty(field)){
	findUrl = "/widget/find?code=" + code +"&field="+ field;
}
if(!isEmpty(url!)){
	// 自定义URL
	findUrl = url;
} else {
	// 自定义表达式
	findUrl = "/widget/find?";
	// 存在自定义表达式
	if(!isEmpty(exp!)){
		findUrl = findUrl + "exp=" + exp; 
	} else {
		if(!isEmpty(code!)){
			findUrl = findUrl + "code="+ code + "&field=" + field;	
		} else {
			findUrl = "";
		}
	}
	findUrl = findUrl + '&template=lay';
	
}

var placeholderstr = "请选择";
if(isTrue(isQuery!)){//查询
	placeholderstr = "请选择"+placeholder;
}else if(!isEmpty(placeholder!) ){
	placeholderstr = placeholder;
}
// 将URL作为属性放置于值所在的隐藏文本框上，方面级联时JS修改URL
%>

<div class="layui-input">
     <input type="text" id="${id!}_show" name="${name}_show"  placeholder="${placeholderstr!}"  ${disabled}   class="tags-input">
	 <input type="hidden" id="${id!}" name="${name}" value="${value!}" lay-verify="${verify!}" title='${item.cn!}'  >
</div>

<script type="text/javascript">
$(function () {

	
	
	
var init${id!}Data = function() {	
	// 手动赋值,如果有值的花
 	$.ajax({
		url : '${STATIC!}'+"/widget/findCnByEn/${code!}-${field!}-${value!}",
		type : 'get',
		success : function(data) {
			
			var objShow = $('#${id!}_show');
			var obj = $('#${id!}');
			
			var object = eval(data);
			var text_field_name = object.text_field;
			var text_field_id = null;
			var objects = eval(object.data);
	
			
			for(var key in objects[0]){ 
				if(key !=text_field_name){
					text_field_id = key;
					break;
				}
			}
			
			
			for(j = 0,len=objects.length; j < len; j++) {
				var one = objects[j];
				if(j == 0)
					addTag(objShow,obj,one[text_field_id],one[text_field_name],true);
				else
					addTag(objShow,obj,one[text_field_id],one[text_field_name],false);
			}
			
			
		}
	});
}
if('${value!}' != ''){
	init${id!}Data();
}
	
		 //如果是只读模式，则事件移除
 		 //tag移除事件
 	 	 $(document).on('click','.${id!}',function(obj){
 	 	      // console.log(obj.target.parentNode);//标准 
 	 	      
 	 	      if($("#${id!}_show").attr('readonly') != "readonly")
 	 	        removeTag(obj.target);
 	 	   });
 	 	 //点击事件
 	 	 $("#${id!}_show").click(function(){
 	 		 if($("#${id!}_show").attr('readonly') != "readonly")
 	 			tagchoice($('#${id!}_show'),$('#${id!}'),'${findUrl!}',${ismultiple!});
 	 	  });



});	    
</script>
