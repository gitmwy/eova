<form class="layui-form">
	<ul class="layer-photos-demo"  id="${id}" lay-filter="${id}"></ul>
</form>

<script>
// var ${'$'+id};
//暂时不考虑父子结构，但是需要考虑跳转过来的
$(function () {

    // init param
    var id = "${id!}";
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
    
    var values = $.getFormParasObj($('#queryForm'));
    if(sortName != null){
    	values['sort'] = sortName;
    	values['order'] = sortOrder;
    }
    console.log(values); 
    
    layui.use(['form','flow','layer','laydate','table','laytpl','element'],function(){
    	var flow = layui.flow,
        form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        upload = layui.upload,
        $ = layui.jquery;
    	
    	var imgNums = limit;  //单页显示图片数量
    	
    	//流加载图片
        var imgNums = 15;  //单页显示图片数量
        flow.load({
            elem: '#${id}', //流加载容器
            done: function(page, next){ //加载下一页
            	
            	//需要自己带上page
            	$.post(url, values, function(res) {
            	
               	// $.get("/layuicms2.0/json/images.json",function(res){
                	res =	eval('(' + res + ')');
                	
                    //模拟插入
                    var imgList = [],data = res.rows;
                    var maxPage = imgNums*page < data.length ? imgNums*page : data.length;
                    maxPage = res.total;
                    setTimeout(function(){
                        for(var i=imgNums*(page-1); i<maxPage; i++){
                        	var one = '<li><img layer-src="/layuicms2.0/'+ data[i].src +'" src="/layuicms2.0/'+ data[i].thumb +'" alt="'+data[i].alt+'"><div class="operate"><div class="check"><input type="checkbox" name="belle" lay-filter="choose" lay-skin="primary" title="'+data[i].alt+'"></div><i class="layui-icon img_del">&#xe640;</i></div></li>';
                           
                        	one = '<li><img layer-src="/layuicms2.0/images/userface4.jpg" src="/layuicms2.0/images/userface4.jpg" alt="美女生活照4"><div class="operate"><div class="check"><input type="checkbox" name="belle" lay-filter="choose" lay-skin="primary" title="美女生活照4"></div><i class="layui-icon img_del">&#xe640;</i></div></li>';
                        	
                        	one = '<li>'
                        		+'<img layer-src="/layuicms2.0/images/userface4.jpg"'
                        		+'	src="/layuicms2.0/images/userface4.jpg" alt="美女生活照4">'
                        		+'<div class="operate">'
                        		+'		<div class="check">'
                        		+'			<input type="checkbox" name="belle" lay-filter="choose" lay-skin="primary" title="美女生活照4">'
                        		+'		</div>'
                        		+'		<i class="layui-icon img_del">&#xe640;</i>'
                        		+'	</div>'
                        		
                        		+'<div class="layui-row">'
                        	
                        		+'  <div class="layui-col-md6">'
                        		+'     属性：自然科学111111111'
                        		+'  </div>'
                        		+'    <div class="layui-col-md6">'
                        		+'    创建时间：2018-12-12 12:00:00'
                        		+'   </div>'
                        		+'  </div>'
                        		+'	</li>';
                        	
                        		
                        	
                        	console.log(one);
                        	imgList.push(one);
                        }
                        next(imgList.join(''), page < (data.length/imgNums));
                        form.render();
                    }, 500);
                });
            }
        });

        //设置图片的高度
        $(window).resize(function(){
            $("#Images li img").height($("#Images li img").width());
        });
    	
    	
    });
  
    
  
      
  	  
  


    
	
    

	
});
</script>
<%if(!isEmpty(object.diy_js!)){%>
<script type="text/javascript" src="${object.diy_js}"></script>
<%}%>