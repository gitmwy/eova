<form class="layui-form">
<!-- 样式问题，id必须为：Images -->
	<ul class="layer-photos-demo"  id="Images" lay-filter="${id}"></ul>
</form>

<script>
// var ${'$'+id};
var ${objectCode!}_refreshflow = null;
//暂时不考虑父子结构，但是需要考虑跳转过来的
$(function () {

    // init param
    var id = "${id!}";
    id = "Images";
    var masterId = "${masterId!}";
    var is_single = "${isSingle!}";
    var $grid = $("#" + id);
    var is
    var page = true;
    var $masterGrid;
    var limit = 15;
    //框架分页有问题，直接给分页最大值
    if(masterId != ""){
    	$masterGrid = $("#" + masterId);
    	page = false;
    	limit = 2147483647;
    }
	
    
    var height = "${height!}";
    if(height == '')
    	height =  'full-40';
    
    
    
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
    //字段属性
    $.each(fields, function (i, f) {
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

    var selectIndex;
    
    
   
    
     
  

${objectCode!}_refreshflow = function() {
	
	//判断是否加载过
	if($("#Images").length>0){
		 $('#Images').empty();
	}
	
	 layui.use(['form','flow','layer','element'],function(){
    	var flow = layui.flow,
        form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        upload = layui.upload,
        $ = layui.jquery;
    	
    	var imgNums = limit;  //单页显示图片数量
    	
    	//流加载图片
       var thisflow = flow.load({
            elem: '#Images', //流加载容器
            done: function(page, next){ //加载下一页
            	
            	var values = $.getFormParasObj($('#queryForm'));
                if(sortName != null){
                	values['sort'] = sortName;
                	values['order'] = sortOrder;
                }
            	
            	//需要自己带上page
            	$.post(url, values, function(res) {
                	
                    //模拟插入
                    var imgList = [],data = res.rows;
                    var maxPage = imgNums*page < data.length ? imgNums*page : data.length;
                    maxPage = res.total;
                    setTimeout(function(){
                        for(var i=imgNums*(page-1); i<maxPage; i++){

                        	var	one = '<li>';
                        		//图片
                        		var imgName = '';
                        		var imgUrl = '';
                        		var imgId = '';
                        		
                        		var pk_key = null;
                        		if(cols_pre[0] != null){
                        			var pk_key = cols_pre[0].field;
                        			
                        			imgId = data[i][pk_key];
                        		}
                        		if(cols_pre[1] != null){
                        			var key = cols_pre[1].field;
                        			imgUrl = data[i][key];
                        			//处理下
                        		}
								if(cols_pre[2] != null){
									var key = cols_pre[2].field;
									imgName = data[i][key];
                        		}
                        		one +='<img layer-src="'+imgUrl+'"'
                        		one +='	src="'+imgUrl+'" alt="'+imgName+'">'
                        		
                        		//名称+选择框
                        		one +='<div class="operate">'
                        		one +='		<div class="check">'
                        		one +='			<input type="checkbox" name="'+pk_key+'" value="'+imgId+'" lay-filter="choose" lay-skin="primary" title="'+imgName+'">'
                        		one +='		</div>'
                        		//one +='		<i class="layui-icon img_del">&#xe640;</i>'
                        		one +='	</div>'
                        		//one +='<div class="layui-row">'
                        		
                        		var cols_temp = [];
                        		var lastCols = null;
                        		//循环属性（一行至多三个，需自行判断组内信息数量）
                        		for(var z = 0;z<cols_other.length;z++){
                        			//cols_temp.length = 0;
                        			
                        			if(lastCols != null && cols_other[z].fieldnum != lastCols.fieldnum){
                        				one = formExtra(one,cols_temp,data[i]);
                        			}
                        			
                        			cols_temp.push(cols_other[z]);
                        			lastCols = cols_other[z];
                        			
                        		}
                        		
                        		if(cols_temp.length > 0){
                        			one = formExtra(one,cols_temp,data[i]);
                        		}
                        		
                        		one += '</li>';
                        	
                        	//console.log(one);
                        	imgList.push(one);
                        }
                        next(imgList.join(''), page < (data.length/imgNums));
                        form.render();
                      //设置图片的高度
                       // $(window).resize(function(){
                            $("#Images li img").height($("#Images li img").width());
                        //});
                    }, 500);
                });
            }
        });
 	
    	console.log(thisflow);
        
    	  $(window).resize(function(){
       		 $("#Images li img").height($("#Images li img").width());
    	 });
    	
    });
	
}
  
${objectCode!}_refreshflow();         
  	  
function  formExtra(one,cols_temp,linedata){
	var group_length = cols_temp.length;
	var one_group_width = 12/group_length;
	//处理cols_temp ，并生成
	if(group_length != 0){
		one +='<div class="layui-row ">'
    	
		
	    $.each(cols_temp, function (i, g) {
	    	one +='  <div class="layui-col-md'+one_group_width+'">';
	    	one +='     '+g.title+':'+linedata[g.field];
	    	one +='  </div>';
		});

	
		one +='  </div>'
	}
	cols_temp.length = 0;
	return one;
}


    
	
    

	
});
</script>
<%if(!isEmpty(object.diy_js!)){%>
<script type="text/javascript" src="${object.diy_js}"></script>
<%}%>