<%
//文本域复选树形选择控件
//http://www.wisdomelon.com/DTreeHelper/
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

var ismultiple = false;
if(isTrue(multiple!)){
	ismultiple = true;
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

//提取额外 initLevel
var initLevel = 2;

%>
<link rel="stylesheet" href="/ui2/src/layui_ext/dtree/dtree.css">
<link rel="stylesheet" href="/ui2/src/layui_ext/dtree/font/dtreefont.css">
<div >
   	 <textarea class="layui-textarea" id="${id!}_show" name="${name}_show" style="${style!}"  placeholder="${placeholderstr!}" ${disabled!} readonly   ></textarea>
	 <input type="hidden" id="${id!}" name="${name}" value="${value!}" lay-verify="${verify!}" title='${item.cn!}'  >
	 <button class="layui-btn layui-normal" id="openTree${id!}_btn">点击选择</button>
</div>
<script type="text/javascript">
$(function () {

	layui.extend({
		dtree: '/ui2/src/layui_ext/dtree/dtree'
	}).use(['element','layer', 'table', 'code' ,'util', 'dtree'], function(){
		var element = layui.element,
		layer = layui.layer,
		table = layui.table,
		util = layui.util,
		dtree = layui.dtree,
		$ = layui.$;
		
		
		

		$("#openTree${id!}_btn").click(function(){
			$.ajax({
				url : '${STATIC!}${queryUrl!}',
				type : 'get',
				success : function(json) {
				
						//data = json.data;
						//转换格式
						var data = $.toTree(json,'p_id','id','title','[{"type": "0", "isChecked": "0"}]');
						
						//启动树
						 layer.open({
							    type: 1, //type:0 也行
							    title: "选择树",
							    area: ["450px", "60%"],
							    content: '<ul id="openTree_${id!}" class="dtree" data-id="0"></ul>',
							    btn: ['确认选择'],
							    success: function(layero, index){
							      var DTree = dtree.render({
							        obj: $(layero).find("#openTree_${id!}"), 
							        //url: "/ui2/test/openTree3.json",
							        data:data,
							        checkbar: true, // 开启复选框
							        
							        checkbarType:"${ismultiple?'self':'only'}", //半选 非复选=》only，复选=》self
							        initLevel: ${initLevel}, //默认2，如果未配置 
							        done: function(data, obj){  //使用异步加载回调(使用接口才会触发)
							         var reportId = $("#${id!}").val();
							          console.log(reportId);
							         dtree.chooseDataInit("openTree_${id!}", reportId); // 初始化复选框的值
							        }
							      });
							      
							      var reportId = $("#${id!}").val();
							         dtree.chooseDataInit("openTree_${id!}", reportId); // 初始化复选框的值
							    },
							    yes: function(index, layero) {
							      var flag = true;
							      var params = dtree.getCheckbarNodesParam("openTree_${id!}"); // 获取选中值
							      if(params.length == 0){
							        layer.msg("请至少选择一个节点",{icon:2});
							        flag = false;
							      }
							      
							      var ids = [], names = [];
							      for(var key in params){
							        var param = params[key];
							        if(param.nodeId == null)
							        	continue;
							        ids.push(param.nodeId);
							        names.push(param.context);
							      }
							      $("#${id!}").val(ids.join(","));
							      $("#${id!}_show").val(names.join(","));
							      if(flag){
							        layer.close(index);
							      }
							    }
							  });
						
					
					
				}
			});
			
			return false;
			 
			});

	});

});


var init${id!}Data = function(idsStr) {
	if(idsStr == null || idsStr=='')
		return;
	$.syncGetJson('${STATIC!}${queryUrl!}', function(data) {
		
		var ids = [], names = [];
		data.forEach(function (item) {
          var ids = idsStr.  split(',');
          for(var i=0;i<ids.length;i++){
        	  if(ids[i] == item['id']){
        		  names.push(item['name']);

        		  break;
        	  }
          }
        });
		
		
		$("#${id!}_show").val(names.join(","));
	});
};
init${id!}Data('${value!}');
</script>