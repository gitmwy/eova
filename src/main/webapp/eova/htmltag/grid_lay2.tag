<table id="${id}" lay-filter="${id}"></table>

<script>
// var ${'$'+id};
$(function () {

    // init param
    var id = "${id!}";
    var masterId = "${masterId!}";
    var is_single = "${isSingle!}";
    var $grid = $("#" + id);
    var is
    var page = true;
    var $masterGrid;
    var limit = 10;
    //框架分页有问题，直接给分页最大值
    if(masterId != ""){
    	$masterGrid = $("#" + masterId);
    	page = false;
    	limit = 2147483647;
    }
	
    
    var height = "${height!}";
    if(height == '')
    	height =  'full-50';
    
    
    
    //if(id.indexof("master"))
    
    var menuCode = '${menuCode!}';
    var objectCode = '${objectCode!}';
    var toolbar = '${toolbar!}';// grid ref toolbar
    var isPaging = eval('${isPaging!true}');// is show pagination
    var url = '${url!}';// diy grid load data url
    var objectJson = '${objectJson!}';// object is json
    var fieldsJson = '${fieldsJson!}';// fiedlds is json
    var configJson = '${configJson!}';// config is json

    if (url == '') {
        url = '/grid/query/' + objectCode;
        if(menuCode != ''){
        	url = url + '-' + menuCode;
        }
    }
    
    
    
 // 自动传递所有参数
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
   
    var cols = [];
    var validators = {};
    // 批量选择框
    //if (!object.is_single) {
        var attr = new Object;
        attr.type = 'checkbox';
        attr.checkbox = 'left';
        attr.width = 50;
        cols.push(attr);
    //}
    // 字段属性
    $.each(fields, function (i, f) {
        if (!f.is_show) {
            // continue;
            return true;
        }

        var attr = new Object;
        attr.field = f.en;
        attr.title = f.cn;
        attr.width = f.width ? f.width : null;//字段长度
        
        if(f.en == object.pk_name){
        	attr.ispk = true;
        }else
        	attr.ispk = false;
        
        
        attr.align='center';
        
        if (f.is_order) {
            attr.sort = true;
        }
        //console.log(f.cn+":"+f.type+":"+f.formatter);
        if (f.formatter != null && f.formatter !='') {
        	attr.templet = function (d) {
        		//加载函数
        		eval('window.fun_'+f.en+' = '+f.formatter);
        		
        		
        		//四个参数（value,row,index,keyName）如果不需要则直接送value
        		return window['fun_'+f.en](d[f.en],d,d[object.pk_name],f.en);
        		//var sss = window['fun_'+f.en](d[f.en],d,d[object.pk_name],f.en);
        		//console .log("sss======="+sss+',,,,'+f.formatter);
        		//return d[f.en];
        	}
        } else {
            // 默认格式化处理
            if (f.type == '布尔框') {
            	//return '<input type="checkbox" name="newsTop" lay-filter="newsTop" lay-skin="switch" lay-text="是|否" '+d.newsTop+'>'
            	
                attr.align = 'center';
                attr.templet = function (d) {
                	
                	var cn = f.cn;
                	//是否XX
                	if(cn.indexOf("是否") != -1){
                		cn = cn.substring(cn.indexOf("是否")+2);
                	}
                	
                    //return '<input type="checkbox" name="'+f.en+'" lay-filter="'+f.en+'" lay-skin="switch" lay-text="是|否" '+d.newsTop+'>';
               		//return '<input type="checkbox" name="'+f.en+'" value="'+d[f.en]+'" lay-skin="switch" lay-text="是|否" lay-filter="'+f.en+'" checked="">';
               		
               	  if(d[f.en] == 1){
                  	return '<input type="checkbox" name="'+f.en+'" value="'+d[f.en]+'" lay-skin="switch" lay-text="是|否" disabled="" lay-filter="'+f.en+'" checked="">';
                  }else{
                  	return '<input type="checkbox" name="'+f.en+'" value="'+d[f.en]+'" lay-skin="switch" lay-text="是|否" disabled="" lay-filter="'+f.en+'" >';
                  }
                };
            }else if(f.type == '图片框'){//图片框
            	attr.align = 'center';
            	attr.templet = function (d) {
            		var value = d[f.en];
            		if (value) {
            	        return '<img src="' + IMG + '/' + value + '" height=40>'
            	    }
            	    return value;
            	}
            }else if(f.type == '文件框'){//文件框
            	attr.align = 'center';
            	attr.templet = function (d) {
            		var value = d[f.en];
            		if (value) {
            			return '<a href="' + FILE + '/' + value + '" target="view_window" >' + $.getFilePathName(value) + '</a>';
            	    }
            		
            		
            	    return value;
            	}
            }
        }
        // Grid Cell Editor,对象和字段允许行内编辑自增，自增长禁止编辑(目前可编辑不支持)
        if (object.is_celledit && f.is_edit && 1==2) {
        	if(object.is_auto && object.is_auto == true){
        		return;
        	}
            var editor = new Object;
            editor.type = f.editor;
            // 构造参数
            editor.options = {};
            if (f.type == '下拉框') {
                editor.options = {
                    url: '/widget/comboJson/' + objectCode + '-' + f.en, valueField: 'id', textField: 'cn', multiple: f.is_multiple
                }
            } else if (f.type == '查找框') {
                editor.options = {
                    url: '/widget/find?code=' + objectCode + '&field=' + f.en + '&multiple=' + f.is_multiple
                }
            } else if (f.type == '日期框') {
                editor.options = {
            		format: 'yyyy-MM-dd'
                }
            }
            editor.options['name'] = f.en;

            attr.editor = editor;

         	// validator
            var rule = '';
            if(f.is_required){
                rule = f.cn + ':required;';
            }
            if(f.validator){
                rule = rule + f.validator;
            }
            if(rule != ''){
    	        validators[f.en] = { rule: rule };
            }
        }
		
       // attr.event='lineClick';
	
        
        cols.push(attr);
    });
    //隐藏 pk {field:'id',title:'ID', display:'none'}
    

  	
    //if(object.pk_name !=null && object.pk_name!=''){
    //	var attr = new Object;
     //   attr.field = "pk_name";
    //    attr.title = object.pk_name;
    //    attr.display = 'none';
   	//	cols.push(attr);
    //}
   
 console.log(cols);

	// 默认排序
	var sortName = null,sortOrder = 'asc';
	if(object.default_order){
		var defaultOrder = object.default_order.split(' ');
		sortName = defaultOrder[0];
		if(defaultOrder.length > 1){
			sortOrder = defaultOrder[1];
		}
	}

    var selectIndex;
    
    var values = $.getFormParasObj($('#queryForm'));
    if(sortName != null){
    	values['sort'] = sortName;
    	values['order'] = sortOrder;
    }
    console.log(values); 
    
    
    //sort:obj.field //排序字段  
    //,order: obj.type
  
    
    console.log("url:"+url); 
    var table = null;
    layui.use(['form','layer','laydate','table','laytpl','element'],function(){
        var form = layui.form,
            layer = parent.layer === undefined ? layui.layer : top.layer,
            $ = layui.jquery,
            laydate = layui.laydate,
            laytpl = layui.laytpl,
            table = layui.table;
		
        
       // var tableId = '${id}';
        //新闻列表
      var tableIns = table.render({
            elem: '#${id}',
            url : url,
            cellMinWidth : 95,
            page : page,
            height : height,
            limit : limit, //默认10 ，如果设置成不分页则值为2147483647
            loading:true,
            limits : [3,5,10,15,20,25],
            id : "${id}Table",
            request: {
            	  pageName: 'page' //页码的参数名称，默认：page
            	  ,limitName: 'rows' //每页数据量的参数名，默认：limit
            	}  ,
            response: {
            	  statusName: 'status' //数据状态的字段名称，默认：code
            	  ,statusCode: 200 //成功的状态码，默认：0
            	  ,msgName: 'msg' //状态信息的字段名称，默认：msg
            	  ,countName: 'total' //数据总数的字段名称，默认：count
            	  ,dataName: 'rows' //数据列表的字段名称，默认：data
            	}  ,
            cols : [cols]
            <% if( isEmpty(masterId!) ){ %>
			,where: values 
			<% } %>
			,done: function(res, curr, count){
          	    //如果是异步请求数据方式，res即为你接口返回的信息。
          	    //如果是直接赋值的方式，res即为：{data: [], count: 99} data为当前页数据、count为数据总长度
          	    //console.log(res);
          	    
				for(var x=0;x<res.rows.length;x++){
          	    	console.log(res.rows[x]); 
          	    	resetPkId(object,res.rows[x]);
          	    }
          	    console.log("is_single:"+is_single);
          	  var tableView = this.elem.next();
          	    //如果是单选，则把全选按钮隐藏,并且选中第一条（如果有数据的话）
          	    if(is_single){

                        // 单选的情况下可以将全选的按钮设置不可用，或者直接干掉
                        var checkboxAllElem = tableView.find('[name="layTableCheckbox"][lay-filter="layTableAllChoose"]');
                        checkboxAllElem.attr('disabled', 'disabled').next().remove();
                        checkboxAllElem.remove();
                        // 因为done会在渲染完成之后所以而已已经渲染出来的checkbox组件不会因为
                        // 原来的checkbox的消失或者修改一些特殊的样式比如类型而改变所以直接js干掉以前生成出来的checkbox
                        tableView.find('[name="layTableCheckbox"]').attr('type', 'radio').next().remove();
                        form.render();
          	    }
          	    
          	 	
          	    
          	    //判断数据加载完成回调方法定义否？
          	  	if(isExitsFunction("dataDone") && masterId == ""){
          	  		console.log("dataDone:"+masterId);
          	  		dataDone(res, curr, count);
          	  	}
          	  }
            
        });

        
        table.on('sort(${id})', function(obj){ //注：tool是工具条事件名，test是table原始容器的属性 lay-filter="对应的值"
        	  console.log(obj.field); //当前排序的字段名
        	  console.log(obj.type); //当前排序类型：desc（降序）、asc（升序）、null（空对象，默认排序）
        	  console.log(this); //当前排序的 th 对象
        	  
        	  //尽管我们的 table 自带排序功能，但并没有请求服务端。
        	  //有些时候，你可能需要根据当前排序的字段，重新向服务端发送请求，从而实现服务端排序，如：
        	  table.reload('${id}Table', {  
        	    initSort: obj //记录初始排序，如果不设的话，将无法标记表头的排序状态。 layui 2.1.1 新增参数  
        	    ,where: { //请求参数  
        	    	sort:obj.field //排序字段  
        	      ,order: obj.type //排序方式  
        	    }  
        	  });  
        	});
        
      //监听单元格事件
  	  /* table.on('tool(${id})', function(obj){
  	    var data = obj.data;
	  	  var data = obj.data;
	      if(obj.event === 'lineClick'){
	    	  console.log(data);
	    	  if(isExitsFunction("lineClick") && masterId == ""){
	    		  lineClick(data.pk_val,data);
        	  	}
	      }
  	  }); */
      
  	  var choiceNum = 0;
  	 //监听表格复选框选择,单选才控制~~
  	  table.on('checkbox(${id})', function(obj){
  		
  	    
  	    //var cid= obj.data.id;
  	    
  	    var checked = obj.data.LAY_CHECKED;
  	    var index = obj.data.LAY_TABLE_INDEX;
  	    var data = obj.data;
  	    //var checkStatus = table.checkStatus('${id}');
  	    
  	    //alert(choiceNum);
  		console.log(obj);	
  
  	    if(is_single){
	  	    
  	    
  	    }
  	    
  	  //判断数据加载完成回调方法定义否？
  	  //obj.data.id;
  	  //obj.data.LAY_CHECKED;
  	  	if(isExitsFunction("checkboxChicked") ){
  	  		checkboxChicked(obj,is_single);
  	  	}
  	    
  	  });
  	     
      
  	  
  	  
    //table.on('checkbox(${id})',function(obj){ // myTable为容器lay-filter设定的值
    // 触发复选框后的回调函数
    //console.log(obj);
	//})
      
        //var $mylist = $("#${id}");
        //var $mybody = $mylist.find("tbody");
       // $mybody.dblclick(function(event){//监听双击
        //	alert($(event.target).closest("tr")[0].outerHTML)
        //});

    })

//     ${'$'+id} = $myGrid;
    
	
	function choiceone(id,index){
		var tableDiv = $("#"+id+"").next();
		
		   var checkCell = tableDiv.find("tr[data-index=" + index + "]").find("td div.laytable-cell-checkbox div.layui-form-checkbox I");
		   
		    if (checkCell.length>0) {
		    	console.log( checkCell );
		    	
		    	
		    	 //console.log( checkbox.nextSbiling );
		    	
		    	//$("input:checkbox[name='layTableCheckbox']:checked")
		    	//checkbox.attr('checked', 'true');
				//checkbox.click();
		       // checkCell.click();
		        
		        
		       
		       return checkCell;
		        //需要判断是否存在其他，
		       // checkCell.click();
		    }
		    
		    return null;
	}
	
    function choiceoneById(id,dataId){
		var tableDiv = $("#"+id+"").next();
		
		   var checkCell = tableDiv.find("tr[data-index=" + index + "]").find("td div.laytable-cell-checkbox div.layui-form-checkbox I");
		   
		    if (checkCell.length>0) {
		    	console.log( checkCell );
		    	
		    	
		    	 //console.log( checkbox.nextSbiling );
		    	
		    	//$("input:checkbox[name='layTableCheckbox']:checked")
		    	//checkbox.attr('checked', 'true');
				//checkbox.click();
		       // checkCell.click();
		        
		        
		       
		       return checkCell;
		        //需要判断是否存在其他，
		       // checkCell.click();
		    }
		    
		    return null;
	}
    
  //额外增加一个参数(主键) pk_val,object.pk_name，id  ，
  //pk_name,pk_id,cn_name
    function resetPkId(object,data){
    	var pk_val = null;
    		pk_val = data['pk_val'];
    	var pk_name = object.pk_name;
    	if(pk_val != null){
    		data.pk_name = object.pk_name;
    		data.pk_id = pk_val;
    	}else if(pk_name !=null && pk_name!=''){
    		data.pk_name = pk_name;
  			data.pk_id = data[pk_name];
    	}else{
    		data.pk_name = 'id';
    		data.pk_id = object.id;
    	}
    	data.cn_name = object.cn;
    }
	
});



</script>