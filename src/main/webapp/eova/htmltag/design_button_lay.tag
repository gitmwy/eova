<%
//用于form 表单添加“设计按钮” ，只有超管开发人员可以设计
/**
参数:
	1、元对象
	2、对象操作form （可以空，则存的时候让输入）  VIEW_FORM，PAGE_TYPE+"Form"
例如：/form/add/o_class?template=lay 即：o_class 操作form：addForm

<#design_button_lay objectCode="${f}" formType="formType" />
**/
%>
<button class="layui-btn formEdit" >
表单设计
</button>
<script type="text/javascript">
//表单编辑
$(".formEdit").click(function(){
	var objectCode = '${objectCode!}';
	var formType = '${formType!}';
	
	if(objectCode.length >0){
		//新窗口打开设计窗口 /form/design/o_class?template=lay
		var url = "/form/design/"+objectCode+"?template=lay";
		if(formType != null && formType.length>0)
			url = url+"&formType="+formType;
		window.open(url);  
	}
	return false;
});
</script>