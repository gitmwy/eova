<%
// 查询控件name自动追加前缀
var name = item.en;
if(isTrue(isQuery!)){
	name = "query_" + name;
}

//字段2种方案，见表eova_widget
//第一 对应的tag 
//第二 在数据库制定类型对应 widget 地址 /widget/password/index.html

//字段属性说明：
//1、id
//2、name
//3、code
//4、field
//5、value
//6、isNon
//7、multiple
//8、isReadonly
//9、validator
//10、placeholder
//11、options 推荐是格式化
//12、style
//13、filedir

//目前数据类型不支持设置98%，所以现在宽度 统一设置98%，后期生效了再允许这个业务
var width = '98%';
/* if(!isEmpty(item.width!)){
	if( strutil.contain(item.width+"","%"))
		width = item.width;
	else
		width = item.width+"px";
} */
if(isTrue(model!)){
	value = "${@record.get(\'"+item.en+"\')}";
}else{
	//可能值是"${xx}"这样的默认值，需要处理
	//debug(value!);
	if(!isEmpty(value!) 
			&& strutil.startWith(value!+'','${') && strutil.endWith(value!+'','}') ){
		//EovaConst.USER
		value =	strutil.toBeetlValue(value!,session.user);
	
	}
}
%>
<%if(item.type == "下拉框"){%>
	<#combo_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}"  code="${item.object_code}" field="${item.en}" value="${value!}" placeholder="${placeholder!item.placeholder!}" isNoN="${item.is_required!}" multiple="${item.is_multiple}" isReadonly="${readnoly!false}" />
<%} else if(item.type == "下拉树"){%>
	<#combo_tree_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}"  code="${item.object_code}" field="${item.en}" value="${value!}" placeholder="${placeholder!item.placeholder!}" isNoN="${item.is_required!}" multiple="${item.is_multiple}" isReadonly="${readnoly!false}" />

<%} else if(item.type == "下拉树2"){%>
	<#tree_select_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}"  code="${item.object_code}" field="${item.en}" value="${value!}" placeholder="${placeholder!item.placeholder!}" isNoN="${item.is_required!}" multiple="${item.is_multiple}" isReadonly="${readnoly!false}" />

<%} else if(item.type == "查找框"){%>
    <#find_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}"  code="${item.object_code}" field="${item.en}" value="${value!}" isNoN="${item.is_required!}" placeholder="${placeholder!item.placeholder!}" multiple="${item.is_multiple}" isReadonly="${readnoly!false}" />
<%} else if(item.type == "时间框"){
	if(isTrue(model!)){
		value = "${strutil.formatDate(@record.get(\'"+item.en+"\'), 'yyyy-MM-dd HH:mm:ss')}";
	}
%>
    <#time_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}" value="${value!}" isNoN="${item.is_required!}" isReadonly="${readnoly!false}" placeholder="${placeholder!item.placeholder!}" options="yyyy-MM-dd HH:mm:ss" />
<%} else if(item.type == "日期框"){
	if(isTrue(model!)){
		value = "${strutil.formatDate(@record.get(\'"+item.en+"\'), 'yyyy-MM-dd')}";
	}%>
    <#time_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}" value="${value!}" isNoN="${item.is_required!}" isReadonly="${readnoly!false}" placeholder="${placeholder!item.placeholder!}" options="yyyy-MM-dd"/>
<%} else if(item.type == "文本域"){%>
	<#texts_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}" value="${value!}" isNoN="${item.is_required!}" placeholder="${placeholder!item.placeholder!}" validator="${item.validator!}" style="width:${width};height:${item.height!340}px;" isReadonly="${readnoly!false}" />
<%} else if(item.type == "编辑框"){%>
	<#edit_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}" value="${value!}" isNoN="${item.is_required!}" placeholder="${placeholder!item.placeholder!}" style="width:${width};height:${item.height!340}px;" width="${width}" height="${item.height!340}" isReadonly="${readnoly!false}" />
<%} else if(item.type == "布尔框"){%>
	<#bool_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}" value="${value!}" options="" placeholder="${placeholder!item.placeholder!}" isReadonly="${readnoly!false}" />
<%} else if(item.type == "图片框"){%>
	<#img_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}" value="${value!}" isNoN="${item.is_required!}" placeholder="${placeholder!item.placeholder!}" filedir="${item.config.filedir!}" isReadonly="${readnoly!false}" />
<%} else if(item.type == "文件框"){%>
	<#file_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}" value="${value!}" isNoN="${item.is_required!}" placeholder="${placeholder!item.placeholder!}"  filedir="${item.config.filedir!}" isReadonly="${readnoly!false}" />
<%} else if(item.type == "图标框"){%>
    <#icon_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}" value="${value!}" isNoN="${item.is_required!}" placeholder="${placeholder!item.placeholder!}" isReadonly="${readnoly!false}" />
<%} else if(item.type == "自增框" || item.type == "文本框" || item.type == "数字框" || item.type == "JSON框"){%>
    <#text_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}" value="${value!}" placeholder="${placeholder!item.placeholder!}" isNoN="${item.is_required!}" validator="${item.validator!}" options="" isReadonly="${readnoly!false}" />
<%} else if(item.type == "JSON框"){//json 暂时并入edit_lay%>
	<#edit_lay f="${item}" defaultValue="${defaultValue!}" id="${item.en}" name="${name}" value="${value!}" isNoN="${item.is_required!}" placeholder="${placeholder!item.placeholder!}" style="width: auto;height:${item.height!250}px;" isReadonly="${readnoly!false}" />
<%} else {// 默认为文本框%>
	<% var widgets = query("widget"); %>
    <%for(widget in widgets){%>
	    <%if(widget.value == item.type){%>
	    <%include(widget.path, {'field' : item,'defaultValue':defaultValue!, 'name' : name!, 'value' : value!, 'isNoN' : item.is_required!, 'validator' : item.validator!,'isReadonly':readnoly!false}){}%>
	    <%}%>
    <%}%>
<%}%>