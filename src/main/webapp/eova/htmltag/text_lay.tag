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
if(isTrue(isReadonly!) ||isTrue(view!)){
	disabled = 'readonly';
	verify = '';
}


%>
<input type="text" id="${id!}" name="${name!}" defaultvalue="${defaultValue!}"  value="${value!}"  placeholder="${placeholder!}" ${disabled!} lay-verify="${verify!}" autocomplete="off" class="layui-input">