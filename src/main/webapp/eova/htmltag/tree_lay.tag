<%
//控件：dtree
//http://47.105.168.195/DTreeHelper/
%>
<script type="text/javascript">

	$(function() {
		// 初始化参数
		var $tree = $("#${id}");
		var masterId = "${masterId!}";
		var $master;
		if (masterId != "") {
			$master = $("#" + masterId);
		}
		var menuCode = '${menuCode!}';
		var objectCode = '${objectCode!}';
		var objectJson = '${objectJson!}'; // object is json
		var fieldsJson = '${fieldsJson!}'; // fiedlds is json
		var configJson = '${configJson!}'; // config is json

		var url = "${url!}";
		var idKey = "${idKey!'id'}";
		var nameKey = "${nameKey!'name'}";
		var pidKey = "${pidKey!'parent_id'}";
		var isExpand = "${isExpand!false}";
		var rootPid = "${rootPid!0}";

		// 初始化组件
		EovaWidget.init(objectCode, objectJson, fieldsJson, configJson);
		var config = EovaWidget.data.config,
			object = EovaWidget.data.object,
			fields = EovaWidget.data.fields;
		// 初始化URL
		if (url == '') {
			url = '/tree/query/' + objectCode;
			if(menuCode != ''){
	        	url = url + '-' + menuCode;
	        }
		}
		// 根据URL获取JSON
		var data;
		$.syncGetJson(url, function(json) {
			data = json;
			
		});
		
		

		
	
	
		var response = {
				  statusName: "code", //返回标识
				  statusCode: 200, //返回码
				  message: "msg", //返回信息
				  rootName: "data", //根节点名称
				  treeId: idKey, //节点ID
				  parentId: pidKey, //父节点ID
				  title: nameKey, //节点名称
				  iconClass: "iconClass", //自定义图标class
				  childName: "children", //子节点名称
				  isLast: "isLast", //是否最后一级节点
				  level: "level", //层级
				  checkArr: "checkArr", //复选框列表
				  isChecked: "isChecked", //是否选中
				  type: "type", //复选框标记
				  basicData: "basicData" //表示用户自定义需要存储在树节点中的数据
				};

		
	//数据需要本地处理下了 
	//http://47.105.168.195/DTreeHelper/

		layui.extend({
			dtree: '/ui2/src/layui_ext/dtree/dtree'
		}).use(['element','layer', 'table', 'code' ,'util', 'dtree'], function(){
			var element = layui.element,
			layer = layui.layer,
			table = layui.table,
			util = layui.util,
			
			$ = layui.$;
			var	dtree = layui.dtree;

			// 根据URL获取JSON
			var data = null;
			$.syncGetJson(url, function(json) {
				if(json.status === 200)
					data = json.data;
			});
			
			var data = $.toTree(data,pidKey,idKey);
			//tree展开层次，不展开=1，否则展开2层
			var initLevel = 1;
			if(isExpand)
				initLevel = 2;
			
			var DemoTree = dtree.render({
		    	  elem: "#${id!}",
		    	  //url: "/ui2/test/commonTree3.json",
		    	  data:data,
		    	  type: "load",
		    	  initLevel: initLevel,
		    	  response :response
		    	});
		    	//dtree.on("node('commonTree1')" ,function(param){
		    	//  layer.msg(JSON.stringify(param));
		    	//});
			

		    
			if(isExitsFunction("treedataDone") ){
      	  		//console.log("dataDone:"+masterId);
      	  		treedataDone();
      	  	}
		});
		
	});
</script>
<div class="dtree left">
<ul id="${id!}" class="dtree" data-id="0"></ul>
</div>