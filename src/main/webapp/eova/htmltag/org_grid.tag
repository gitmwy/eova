<!-- 组织结构 -->
 <link rel="stylesheet" href="/plugins/OrgChart/css/font-awesome.min.css">
<link rel="stylesheet" href="/plugins/OrgChart/css/jquery.orgchart.css">
  <script type="text/javascript" src="/plugins/OrgChart/js/jquery.orgchart.js"></script>
  <style type="text/css">
    .orgchart .second-menu-icon {
      transition: opacity .5s;
      opacity: 0;
      right: -5px;
      top: -5px;
      z-index: 2;
      color: rgba(68, 157, 68, 0.5);
      font-size: 18px;
      position: absolute;
    }
    
    .orgchart .second-menu-icon2 {
      transition: opacity .5s;
      opacity: 0;
      right: -5px;
      top: -5px;
      z-index: 2;
      color: rgba(68, 157, 68, 0.5);
      font-size: 18px;
      position: absolute;
    }
    
    .orgchart .second-menu-icon:hover { color: #449d44; }
    .orgchart .node:hover .second-menu-icon { opacity: 1; }
    .orgchart .node .second-menu {
      display: none;
      position: absolute;
      top: 0;
      right: -70px;
      border-radius: 35px;
      box-shadow: 0 0 10px 1px #999;
      background-color: #fff;
      z-index: 1;
    }
    .orgchart .node .second-menu .avatar {
      width: 60px;
      height: 60px;
      border-radius: 30px;
      float: left;
      margin: 5px;
    }
  </style>
   <style type="text/css">

    .orgchart { background: #fff; }
    .orgchart.edit-state .edge { display: none; }
    .orgchart .node { width: 150px; }
    .orgchart .node .title { height: 30px; line-height: 30px; }
    .orgchart .node .title .symbol { margin-top: 1px; }
    .orgchart {
  background: #fff;
  border: 0;
  padding: 0;
  width:100%;
}
    
    #edit-panel {
      position: relative;
      left: 10px;
      width: calc(100% - 40px);
      border-radius: 4px;
      float: left;
      margin-top: 10px;
      padding: 10px;
      color: #fff;
      background-color: #449d44;
    }
    #edit-panel .btn-inputs { font-size: 24px; }
    #edit-panel.edit-state>:not(#chart-state-panel) { display: none; }
    #edit-panel label { font-weight: bold; }
    #edit-panel.edit-parent-node .selected-node-group { display: none; }
    #chart-state-panel, #selected-node, #btn-remove-input { margin-right: 20px; }
    #edit-panel button {
      color: #333;
      background-color: #fff;
      display: inline-block;
      padding: 6px 12px;
      margin-bottom: 0;
      line-height: 1.42857143;
      text-align: center;
      white-space: nowrap;
      vertical-align: middle;
      -ms-touch-action: manipulation;
      touch-action: manipulation;
      cursor: pointer;
      -webkit-user-select: none;
      -moz-user-select: none;
      -ms-user-select: none;
      user-select: none;
      background-image: none;
      border: 1px solid #ccc;
      border-radius: 4px;
    }
    #edit-panel.edit-parent-node button:not(#btn-add-nodes) { display: none; }
    #edit-panel button:hover,.edit-panel button:focus,.edit-panel button:active {
      border-color: #eea236;
      box-shadow:  0 0 10px #eea236;
    }
    #new-nodelist {
      display: inline-block;
      list-style:none;
      margin-top: -2px;
      padding: 0;
      vertical-align: text-top;
    }
    #new-nodelist>* { padding-bottom: 4px; }
    .btn-inputs { vertical-align: sub; }
    #edit-panel.edit-parent-node .btn-inputs { display: none; }
    .btn-inputs:hover { text-shadow: 0 0 4px #fff; }
    .radio-panel input[type='radio'] { display: inline-block;height: 24px;width: 24px;vertical-align: top; }
    #edit-panel.view-state .radio-panel input[type='radio']+label { vertical-align: -webkit-baseline-middle; }
    #btn-add-nodes { margin-left: 20px; }
  </style>
<div  id="${id}" lay-filter="${id}"></div>
<input type="hidden" id="${id}_selected-node" class="selected-node-group">
<input type="hidden" id="${id}_node-nums" class="selected-node-group" value="0">
<script>
// var ${'$'+id};
var ${id}_refreshOrg = null;
//暂时不考虑父子结构，但是需要考虑跳转过来的
$(function () {

    // init param
    var id = "${id!}";
    var masterId = "${masterId!}";
    var is_single = "${isSingle!}";
    var $grid = $("#" + id);
    var page = true;
    var $masterGrid;
    var limit = 15;
    //框架分页有问题，直接给分页最大值
    if(masterId != ""){
    	$masterGrid = $("#" + masterId);
    	page = false;
    	limit = 2147483647;
    }
	
    
    var nameKeyStr = "${nameKey}";//可能多个（最多2个）
    var iconKey = "${iconKey}";//可能空
   
    var nameKeys =  nameKeyStr.split(",");
    var menuCode = '${menuCode!}';
    var objectCode = '${objectCode!}';
    var idKey="${idKey}";
    var pidKey = "${pidKey}"; //存在此字段被转意情况，。即：pid=》pid(名字) 和 pid_val
    
    //判断模式，如果有图片则图片+小字，否则使用文字（最多2个 部门、负责人？）
    var mode = 1;//1-图片，2-文字
    var nameKeysLength = 1;
    nameKeysLength = nameKeys.length;
    if(iconKey != '' && iconKey!= null){
    	mode = 1;
    }else{
    	mode = 2;
    }
    
    

    var url = '${url!}';// diy grid load data url
    var objectJson = '${objectJson!}';// object is json
    var fieldsJson = '${fieldsJson!}';// fiedlds is json
    var configJson = '${configJson!}';// config is json
	
    var treeJson = '${menu.config.tree}';
    //debug(configJson!);
    if (url == '') {
        url = '/org_grid/query/' + objectCode;
        if(menuCode != ''){
        	url = url + '-' + menuCode;
        }
    }
    
    
    
 	//自动传递所有参数
    // 是否含有关联查询条件
    if(masterId == ""){//
    	var paras = $.getUrlParas();
    	if(paras && (paras.indexOf('query_') != -1 || paras.indexOf('filter_') != -1)){
        	url = url + '?' + paras;
        } 
    }else{
    	//设置为空，等待页面选中第一条
    	url = "";
    }

    // console.log(objectCode + 'isFirstLoad' + isFirstLoad);

	// 初始化组件
	EovaWidget.init(objectCode, objectJson, fieldsJson, configJson);
    var config = EovaWidget.data.config,
    	object = EovaWidget.data.object,
    	fields = EovaWidget.data.fields;
	
    if(is_single == '' )
		is_single = object.is_single;
    
    // 当前对象是否允许初始加载数据
    var isFirstLoad = false;
    var isFirstLoadNow = eval('${isFirstLoad!true}');
	// 必须当前业务和对象都允许加载数据
    if(isFirstLoadNow && object.is_first_load){
    	isFirstLoad = true;
    }

    var cols_pre = new Array(3);//第一个为pk，第2个为图片，第3位名称（可空），
    var cols_other = []; //剩下的按组排列（一组推荐最大3个）
    var validators = {};
  	
    var firstGroupId = -1;
    var cGroupId = -1;
    var cGroupNums = -1;
    
    var pkField = null;
    
    //字段属性
    $.each(fields, function (i, f) {
    	
    	if(pidKey == f.en && (f.exp != '' && f.exp !=null)){
    		pidKey = pidKey+'_val';
    	}
    	
    	
        if (!f.is_show) {
            // continue;
            return true;
        }
		
        if(firstGroupId == -1)
        	firstGroupId = f.fieldnum;
        
        if(cGroupId != f.fieldnum){
        	cGroupId = f.fieldnum;
        	cGroupNums = 1;
        }else
        	cGroupNums++;
        
        var attr = new Object;
        attr.field = f.en;
        attr.title = f.cn;
        
        attr.fieldtype = f.type;
        attr.fieldnum = f.fieldnum;//组序号
        
        if(f.en == object.pk_name){
        	attr.ispk = true;
        	pkField = f;
        }else
        	attr.ispk = false;
        
        
        
      
        //console.log(f.cn+":"+f.type+":"+f.formatter);
        if (f.formatter != null && f.formatter !='') {
        	attr.templet = function (d) {
        		//console.log(f.formatter);
        		//加载函数
        		eval('window.fun_'+f.en+' = '+f.formatter);
        		//四个参数（value,row,index,keyName）如果不需要则直接送value
        		return window['fun_'+f.en](d[f.en],d,d[object.pk_name],f.en);
        	}
        } else {
            if(f.type == '图片框'){//图片框
            	
            	attr.align = 'center';
            	attr.templet = function (d) {
            		var value = d[f.en];
            		if (value) {
            			if(value.indexOf('http://') == -1)
            				value = FILE + '/' + value;
            			
            			value = $.getTableLineImg(value,'',$.getFilePathName(value));
            	    }
            	    return value;
            	}
            }
        }
	
        ////第一个为pk，第2个为图片，第3位名称（可空），
        if(attr.ispk == true){//id
        	cols_pre[0] = attr;
        }else if(firstGroupId == f.fieldnum && f.type == '文本框'){//name,第一组内，类型是文本 
    		cols_pre[2] = attr;
        
        }else if(firstGroupId == f.fieldnum && f.type == '图片框'){//img url 第一组内，类型是图片
			cols_pre[1] = attr;
		}else if(firstGroupId != f.fieldnum && cGroupNums<=3){//一组最多只能三个
			attr.groupNums = cGroupNums;//最后一组才是对的吧
		
			cols_other.push(attr);
		
        }else{
        	
        }
    });
  
    



	// 默认排序
	var sortName = null,sortOrder = 'asc';
	if(object.default_order){
		var defaultOrder = object.default_order.split(' ');
		sortName = defaultOrder[0];
		if(defaultOrder.length > 1){
			sortOrder = defaultOrder[1];
		}
	}
	
	//显示数据两种模式：图片+名称/字段。。。,目前现做
	var nodeTemplate = function(data) {
	      return `
	        <span class="office">data.city</span>
	        <div class="title">data.name</div>
	        <div class="content">data.title</div>
	      `;
	    };
	    

	    layui.use(['form','layer','laydate','table','laytpl','element'],function(){
	        form = layui.form;
	    });
	    
var oc_${id} = null;
${id}_refreshOrg = function() {
	var values = $.getFormParasObj($('#queryForm'));
    if(sortName != null){
    	values['sort'] = sortName;
    	values['order'] = sortOrder;
    }
    var datascource = null;
    
	//需要自己带上page
  	$.post(url, values, function(res) {		
  		datascource = $.toTree(res.data,pidKey,idKey);
  		
  	
  		
  		console.log(datascource);
  		//节点数
  		$('#${id}_node-nums').val(res.count);
  		
  		//未空不需要画图，否则组件会进入死循环111
  		if(res.count == 0){
  			return;
  		}
  		datascource = datascource[0];
  		if(mode == 1){//图片(暂时不处理)
  			
  			
  			
  		}else if(mode == 2){//文字（1-2个文字部分）
  			if(oc_${id} == null)
	  			oc_${id} =   $('#${id}').orgchart({
	  	  	        'data' : datascource,
	  	  	     //   'visibleLevel': 2, //默认显示等级
	  	  	        ///'nodeTemplate': nodeTemplate,
	  	  	        // 'direction':'l2r',//"左到右 默认从上到下
	  	  	        'nodeID': '${idKey!}',
	  	  	        'chartClass': 'edit-state',//设置不允许类似箭头钻取
	  	  	      	'nodeTitle':nameKeys[0],
	  	  	      	<% if(strutil.contain(nameKey,',')){ %>
	  	  	    	'nodeContent':nameKeys[1],
	  	  	    	<% } %>
	  	  	      
	  	  	        'exportFilename': 'SportsChart',
	  	  	        'parentNodeSymbol': 'fa-th-large',
	  	  	        
	  	  	        'createNode': function($node, data) {
	  	  	        	//增加权限按钮（估计还是在第一排加权限按钮）
	  	  	          var secondMenuIcon = $('<i>', {
	  	  	            'class': 'fa fa-info-circle second-menu-icon',
	  	  	            click: function() {
	  	  	              $(this).siblings('.second-menu').toggle();
	  	  	            }
	  	  	          });
	  	  	        	
	
	  	  	          var secondMenu = '<div class="second-menu"><button class="layui-btn layui-btn-sm aaaa" title="新增"><i class="layui-icon">&#xe654;</i></button></div>';
	  	  	          //$node.append(secondMenuIcon).append(secondMenu);
	  	  	        }
	  	  	      }); 
  			else{
  				oc_${id}.init({ 'data': datascource });
  			}
  		}
  		
  		 oc_${id}.$chartContainer.on('click', '.node', function() {
  	        var $this = $(this);
  	        var aa = $this.attr("id");
  	        $('#${id}_selected-node').val($this.attr("id"));//.data('node', $this);
  	      });

  	      oc_${id}.$chartContainer.on('click', '.orgchart', function(event) {
  	        if (!$(event.target).closest('.node').length) {
  	          $('#${id}_selected-node').val('');
  	        }
  	      });
  	    $('#${id}_selected-node').val('');
  	    
  	    console.log(oc_${id});
  	});
	
	
	
	
	 
 	
    	
   
	
}
  
${id}_refreshOrg();         
  	 
	
});
</script>
<%if(!isEmpty(object.diy_js!)){%>
<script type="text/javascript" src="${object.diy_js}"></script>
<%}%>