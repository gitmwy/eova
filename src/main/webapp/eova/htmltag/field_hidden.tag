<%
// 隐藏项输出
if(!isEmpty(fixed!)){
						// JSON对象化
						var fix = parseJson(fixed);
						var fixedValue = fix[item.en];
						// 固定值覆盖动态值并且只读
						if(!isEmpty(fixedValue)){
							value = fixedValue;
							//isReadOnly = true;
						}
}

var name = item.en;
if(isTrue(model!)){
	value = "${@record.get(\'"+item.en+"\')}";
}else{
	//可能值是"${xx}"这样的默认值，需要处理
	if(!isEmpty(value!) 
			&& strutil.startWith(value!+'','${') && strutil.endWith(value!+'','}') ){
		//EovaConst.USER
		value =	strutil.toBeetlValue(value!,session.user);
	
	}
}
%>
<%if(item.type == "时间框"){
	debug(value!);
	if(isTrue(model!)){
		value = "${strutil.formatDate(@record.get(\'"+item.en+"\'), 'yyyy-MM-dd HH:mm:ss')}";
	}else if(!isEmpty(value!)){
		value = strutil.formatDate(value!,'yyyy-MM-dd HH:mm:ss');
	}
%>
<input type="hidden" id="${name}" name="${name}" value="${value!}" />
<% }else if(item.type == "日期框"){ 
	if(isTrue(model!)){
		value = "${strutil.formatDate(@record.get(\'"+item.en+"\'), 'yyyy-MM-dd')}";
	}else if(!isEmpty(value!)){
		value = strutil.formatDate(value!,'yyyy-MM-dd');
	}
%>
<input type="hidden" id="${name}" name="${name}" value="${value!}" />
<% }else{//其他控件 %>
<input type="hidden" id="${name}" name="${name}" value="${value!}" />
<% } %>