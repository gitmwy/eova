<%
//图片或者文件 三个路径  1、基本路径 http://127.0.0.1:8080/file 2、内部路径如：/avatar/ ,3、文件名 1475647037704.jpg ,需要改成数据库存储：/avatar/1475647037704.jpg 因为内部路径可以能程序调整发生变化
//一个完整的访问路径：http://127.0.0.1:8080/file/avatar/1475647037704.jpg

if(isEmpty(filedir!)){
	filedir = "/"+objectCode!+"/"+name!;
}

var uploadDir = filedir;
debug(filedir);
var imgUrl = "";
if(!isEmpty(value!)){
	if(isEmpty(uploadDir!)){
		//uploadDir = "temp";
		//uploadDir = com.eova.common.utils.xx.getConfig("eova_upload_temp");
	}
	//imgUrl = IMG! + uploadDir + '/' + value;
	
	if(strutil.index(value,'http://') == -1){
		imgUrl = IMG! +  '/' +  value;
	}else
		imgUrl =  value;
	debug(imgUrl);
}

var verify = "";
if(isTrue(isNoN!) ){//文件只检查是否存在即可
	verify = verify + "required";
}
var disabled="";
if( isTrue(isReadonly!) ||isTrue(view!)){
	disabled = 'disabled=""';
	verify = '';
}
%>


<div class="layui-upload">
  <button type="button" class="layui-btn" id="${id!}_button" name="${name!}_button" ${disabled!}>上传图片</button>
  <div class="layui-upload-list">
  	<input type="hidden"  id="${id!}" name="${name!}" value="${value!}" lay-verify="${verify!}" title='${item.cn!}'/>
    <img class="layui-upload-img" id="${id!}_img" src="${imgUrl!}" >
    <p id="${id!}_text"></p>
  </div>
</div>  


<script>

<% if(!isEmpty(imgUrl)){ %>
$('#${id!}_img').attr('style', 'height: 100px;max-width: 360px;'); 
<% } %>


layui.use('upload', function(){
	  var $ = layui.jquery
	  ,upload = layui.upload;
	  

	  //普通图片上传
	  var uploadInst_${id!} = upload.render({
	    elem: '#${id!}_button'
	    ,url: '${STATIC!}/upload/img?name=${name!}_file&filedir=${filedir}'
	    ,size: IMG_MAX //限制文件大小，单位 KB
	    ,accept:'file'
	    ,exts:UPLOAD_IMG_TYPE
	    ,acceptMime: 'image/*' //（只显示图片文件） 2.2.6 开始新增
	    ,field:'${name!}_file'
	    ,before: function(obj){
	      //预读本地文件示例，不支持ie8
	      obj.preview(function(index, file, result){
	    	$('#${id!}_img').attr('style', 'height: 100px;max-width: 360px;'); 
	        $('#${id!}_img').attr('src', result); //图片链接（base64）
	      });
	    }
	    ,done: function(res){
	      //如果上传失败
	      if(res.success == false){
	        return layer.msg('上传失败');
	      }
	      //上传成功
	      $('#${id!}').attr('value', res.fileName); 
	    }
	    ,error: function(){
	      //演示失败状态，并实现重传
	      var demoText = $('#${id!}_text');
	      demoText.html('<span style="color: #FF5722;">上传失败</span> <a class="layui-btn layui-btn-mini img-reload_${id!}">重试</a>');
	      demoText.find('.img-reload_${id!}').on('click', function(){
	        uploadInst_${id!}.upload();
	      });
	    }
	  });
});


$(function() {
	var $input = $('#${id!}');
	
	var htmlOptions = eval('({${options!}})');
	if (htmlOptions.isReadonly) {
        $input.parent().mask();
        return;
    }
	
	// init input file
	$('.eova-file').each(function() {
		var offset = $(this).find('input[type=text]').offset();
		$(this).find('input[type=file]').css({
			left : offset.left-1,
			// top : offset.top-1,
			width : $(this).width()
		});
	});
	
	// 异步传图(动态绑定事件)
	var myfun = function(){
		
		var $this_file = $("#${id!}_file");
		console.log($this_file.val());
		$input.val($this_file.val());
		var reader = new FileReader();
		var files = $this_file.prop('files');
		reader.readAsDataURL(files[0]);
		reader.onload = function(e) {
			$this_file.parent().find('img').attr('src', e.target.result);
		}
	
		$input.val("Loading...");
		
		$('#${id!}_file').upload({
			action: '/upload/img?name=${name!}_file&filedir=${filedir}',
			name: "${name!}_file",
			onsuccess: function(json) {
				$input.val(json.fileName);
			}
		});
	};
	
	$(document).on("change","#${id!}_file", myfun);
	
});
</script>