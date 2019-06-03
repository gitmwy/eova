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
if( isTrue(isReadonly!) ||isTrue(view!)){
	disabled = 'readonly';
	verify = '';
}


%>
<textarea class="layui-textarea" id="${id!}" name="${name}" style="${style!}"  placeholder="${placeholder!}" ${disabled!} lay-verify="${verify!}"  >${value!}</textarea>

