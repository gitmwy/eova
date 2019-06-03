<%
// data-options
//下啦树选择框
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
//目前有问题的地方在于 添加的事件的地方，通过样式名称来处理，但是没限制范围，导致不能增加多个下拉树
%>
<div class="layui-unselect layui-form-select downpanel selecttree">
                <div class="layui-select-title">
                    <span class="layui-input layui-unselect" id="${id!}_show" name="${name}_show" ${disabled!}>${placeholderstr!}</span>
                     <input type="hidden" id="${id!}" name="${name}" value="${value!}" lay-verify="${verify!}" title='${item.cn!}'  >
                    <i class="layui-edge"></i>
                </div>
                <dl class="layui-anim layui-anim-upbit">
                    <dd>
                        <ul id="${id!}_tree"></ul>
                    </dd>
                </dl>
</div>
<script type="text/javascript">
$(function () {

	
	var init${id!}Data = function() {	
		// 手动赋值,如果有值的花
	 	$.ajax({
			url : '${STATIC!}${queryUrl!}',
			type : 'get',
			success : function(data) {
				
				var objShow = $('#${id!}_show');
				var obj = $('#${id!}');
				
				//select areaId ID,areaName NAME,parentid p_id from table//如果存在ID，CN之外的字段则认为是关联的,并且指定了父节点是谁
				var objects = eval(data);

				var pidKey = null;//夫节点名
				if(objects.length>=1){//这个地方再看下
					var object = objects[0];
					for ( i in object)
					{
						//console.log(i);
						if(i != 'id' && i != 'name'){
							pidKey = i;
							break;
						}
					}
				}
				
				var cvalue = $("#${id!}").val() ;
				if(cvalue != '' && cvalue != null){
					 objects.forEach(function (item) {
						if(item.id == cvalue){
							$("#${id!}_show").text(item.name);
							return false;
						}
			        }); 
				}
				//$("#${id!}_show").val("hahaha");
				
				//删除节点后，后续阶段未处理，所以只提取第一个节点
				var treeObjects = $.toTree(objects,pidKey);
				var val = [];
				val.push(treeObjects[0]);
				val.forEach(function (item) {
					item['spread'] = true;
		        });
				
				layui.use(['element', 'tree', 'layer', 'form', 'upload'], function () {
					var $ = layui.jquery, tree = layui.tree;
					tree({
			            elem: "#${id!}_tree" ,
			            nodes: val, //字段：id,name,children[] 
			            click: function (node) {
			                var $select = $($(this)[0].elem).parents(".layui-form-select");
			                $select.removeClass("layui-form-selected").find(".layui-select-title span").html(node.name).end().find("input:hidden[id='${id!}']").val(node.id);
			           		console.log(node);
			           		console.log($select);
			            }
			        });
			        
					//如果是只读模式，则不加载事件
					if( $("#${id!}_show").attr('readonly') != "readonly"){
				        $(".downpanel").on("click", ".layui-select-title", function (e) {
				            $(".layui-form-select").not($(this).parents(".layui-form-select")).removeClass("layui-form-selected");
				            $(this).parents(".downpanel").toggleClass("layui-form-selected");
				            layui.stope(e);
				        }).on("click", "dl i", function (e) {
				            layui.stope(e);
				        });
				        $(document).on("click", function (e) {
				            $(".layui-form-select").removeClass("layui-form-selected");
				        });
					}
					
				});	
				
			}
		});
	}
	

	init${id!}Data();	
	

	

 	
});
</script>