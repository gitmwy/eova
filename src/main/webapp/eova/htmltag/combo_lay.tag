<%
// data-options
var data = "";
if(!isEmpty(options!)){
	data = data + options;
}

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
if( isTrue(isReadonly!) || isTrue(view!)){
	disabled = 'readonly';
	verify = '';
}

var ismultiple = "";
if(isTrue(multiple!)){
	ismultiple = "multiple";
}
var placeholderstr = "请选择";
if(isTrue(isQuery!)){//查询
	placeholderstr = "请选择"+placeholder;
}else if(!isEmpty(placeholder!) ){
	placeholderstr = placeholder;
}
debug(f!);
var limit = "";//限制选择的项目（其他的不允许选择）
if(!isEmpty(f.limit!) ){
	limit = f.limit!;
}
// 默认URL
var queryUrl = "";
// 编码 或 表达式 必须存在其一
if(!isEmpty(code) || !isEmpty(exp)){
	// 存在自定义URL
	if(!isEmpty(url!)){
		// 自定义URL
		queryUrl = url;
	} else {
		queryUrl = "/widget/comboJson";
		if(!isEmpty(code) && !isEmpty(field)){
			// 存在编码和字段
			queryUrl = queryUrl +"/"+  code +"-"+ field;
		} else if(!isEmpty(exp)){
			// 存在自定义表达式
			queryUrl = queryUrl + "?exp=" + exp; 
		}
	}
}

// 将URL作为属性放置于值所在的隐藏文本框上，方面级联时JS修改URL url="${queryUrl!}"
//控件使用方法 https://github.com/yelog/layui-select-multiple
//lay-verify="${verify!}"
%>
<select  id="${id!}" name="${name!}" value="${value!}" lay-filter="${name!}" url="${queryUrl!}" lay-verify="${verify!}" ${disabled!}   ${ismultiple} lay-search >
<!--         <option value="">${placeholderstr!}</option> -->
       
<!--         <option value="0">写作</option> -->
<!--         <option value="1" >阅读</option> -->
<!--         <option value="2">游戏</option> -->
<!--         <option value="3">音乐</option> -->
<!--         <option value="4">旅行</option> -->
        
      </select>
<script type="text/javascript">
var ${id!}_init= function(isFirst,defaultValue){
	//清除本节点数据（子节点也要执行初始）
	$("#${id!}").empty();
	
	if(isFirst == null)
		isFirst = false;
	
	// 手动赋值(可能存在级联关系) 第一次不管怎么样都执行，不是第一次则根据pid确定是否要执行
	if( !isFirst ){
		var parent = $('select[id="${id!}"]').attr("pid");
		
		if(typeof(parent) != "undefined"){
			var parentVal = $('select[id="'+parent+'"]').val();
			console.log(parent+","+parentVal);
			if(parentVal == '' || parentVal ==null){
				
				renderForm();
				return;
			}
				
		}
	}
	var limit = null;
	if("${limit!}" != ""){
		limit = "${limit!}".split(','); 
	}

 	$.ajax({
		url : '${STATIC!}${queryUrl!}',
		type : 'get',
		success : function(data) {
			
			var objects = eval(data);
			var pidKey = null;
			var pidValue = null;
			//判断是否有
			//select areaId ID,areaName CN,parentid p_id from table//如果存在ID，CN之外的字段则认为是关联的,并且指定了父节点是谁
			if(objects.length>=2){//
				var object = objects[1];
				for ( i in object)
				{
					//console.log(i);
					if(i != 'id' && i != 'cn'){
						pidKey = i;
						break;
					}
				}
			}
			
			if(pidKey != null){
				 //如果存在 
				 if(typeof($('select[id="${id!}"]').attr("pid")) == "undefined" ){
					 $('select[id="${id!}"]').attr("pid",pidKey);
				 }
				pidValue = $('select[id="'+pidKey+'"]').val();
			}
			//检测父节点是否有值才渲染|| 无父节点也渲染
			console.log('pidValue:'+pidValue);
			if(pidKey == null
					|| (pidValue != null && pidValue!= '')){
				
			
				
				$(objects).each(
						function(index) {
							if(index == 0){							
								$("#${id!}").append('<option value="">${placeholderstr!}</option>');
							}
							
							
							
							
							
							var object = objects[index];
							
							//disabled
							var disabled = "";
							if(limit != null){
								var limitOneIndex = $.findArray(limit,object.id);
								if(limitOneIndex==-1)
									disabled = "disabled";	
							}
							if(pidKey == null){
								var optionStr = "";
								
								
								optionStr += "<option value='" + object.id + "' "+disabled+">"
										+ object.cn + "</option>";
								$("#${id!}").append(optionStr);
							}else{
								 if(object[pidKey] == pidValue){
									var optionStr = "";
									optionStr += "<option value='" + object.id + "' "+disabled+">"
											+ object.cn + "</option>";
									$("#${id!}").append(optionStr);
								}
							}
						});
				
				if(defaultValue)
				//if('${value!}' != '')
			    	$('select[id="${id!}"]').val(defaultValue.split(','));
				
			}
			renderForm();//由于前部还有一个重置options的操作
			
			//检查是否有子节点需要刷新数据
			  var pid = $('select[pid="${id!}"]');
			  if(pid.length != 0){
				  //关联初始
				  var funName = pid.attr("id")+"_init";
				  //${id!}_init
				  if(isExitsFunction(funName) ){
					  //funName();
					  //alert(funName);
		    	  		//dataDone(res, curr, count,is_single);
					  eval(funName+"();");

		    	  	}
			  }
		}
	});
}	
	//单节点||级联节点第一节点 需要主动初始
	//var son = $('select[pid="${id!}"]');
	//var parent = $('select[pid="${id!}"]');
	${id!}_init(true,"${value!}");
	
	
 	layui.use(['form'], function(){
 		var form = layui.form;
 		//选中事件
		form.on('select(${name!})', function(data){
			  console.log(data);
			  //检查是否有子节点需要刷新数据
			  var pid = $('select[pid="${id!}"]');
			  if(pid.length != 0){
				  //关联初始
				  var funName = pid.attr("id")+"_init";
				  //${id!}_init
				  if(isExitsFunction(funName) ){
					  //funName();
					  //alert(funName);
	          	  		//dataDone(res, curr, count,is_single);
					  eval(funName+"();");

	          	  	}
			  }
			  
			});
 	});
</script>