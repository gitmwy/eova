<%
//图片或者文件 三个路径  1、基本路径 http://127.0.0.1:8080/file 2、内部路径如：/avatar/ ,3、文件名 1475647037704.jpg ,需要改成数据库存储：/avatar/1475647037704.jpg 因为内部路径可以能程序调整发生变化
//一个完整的访问路径：http://127.0.0.1:8080/file/avatar/1475647037704.jpg
//需要显示图片原名：例如：文件字段叫：xx，则默认(目前只能默认除非改这边的源码)的文件名字段为：xx_name

var fileNameKey = id!+'_name';

//文件真的原名
var srcfileName = null;
if(data! != null)
	srcfileName = @data.get(fileNameKey);

if(isEmpty(filedir!)){
	filedir = "/"+objectCode!+"/"+name!;
}

var uploadDir = filedir;
debug(filedir);
var fileUrl = "";
if(!isEmpty(value!)){
	if(isEmpty(uploadDir!)){
		//uploadDir = "temp";
		//uploadDir = com.eova.common.utils.xx.getConfig("eova_upload_temp");
	}
	//imgUrl = IMG! + uploadDir + '/' + value;
	fileUrl = FILE! + '/' +  value;
	debug(fileUrl);
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
if( isTrue(isReadonly!) ||isTrue(view!)){
	disabled = 'disabled=""';
	verify = '';
}

var fileTypes = "";
if(!isEmpty(item.config.fileTypes!)){
	fileTypes = item.config.fileTypes!;
}
var maxLength = "";
if(!isEmpty(item.config.maxLength!)){
	maxLength = item.config.maxLength!;
}

%>
<div class="upload">
  <button type="button" class="layui-btn" id="${id!}_button" name="${name!}_button" ${disabled!}>${isEmpty(placeholder!)?'选择文件':placeholder!}</button>
  <div class="layui-upload-list">
  	<input type="hidden"  id="${id!}" name="${name!}" title='${item.cn!}' lay-verify="${verify!}" value="${value!}" />
    <p id="${id!}_text"></p>
    
  </div>
</div>  

<script>
// 目前只支持单文件

layui.use( ['upload','layer'], function(){
	  var $ = layui.jquery
	  ,upload = layui.upload;
	  var layer = layui.layer;
	  
	  //初始
	  <% if(!isEmpty(fileUrl!)){ %>
	  showFile_${id!}('${value!}', ' ${!isEmpty(srcfileName!)?srcfileName!:value!}');
	  <% } %>
	  
	  //可能有私有的文件控制
	  var fileTypes = "${fileTypes!}";
	  if(fileTypes == "")
		  fileTypes = UPLOAD_FILE_TYPE;
	  else
		  fileTypes = fileTypes.replaceAll('\\.', '');
	  
	  var maxLength = "${maxLength!}";
	  if(maxLength == "")
		  maxLength = FILE_MAX;
	  
	  var file_loadindex = 0;
	  
	  //普通文件上传
	  var uploadInst_${id!} = upload.render({
	    elem: '#${id!}_button'
	    ,url: '${STATIC!}/upload/file?name=${name!}_file&filedir=${filedir}'
	    ,size: maxLength //限制文件大小，单位 KB
	    ,accept:'file'
	    ,exts:fileTypes
	    ,field:'${name!}_file'
	    ,before: function(obj){
	      //预读本地文件示例，不支持ie8
	      obj.preview(function(index, file, result){
	    	  var demoText = $('#${id!}_text');
	    	  
	    	  file_loadindex = layer.load(1); //添加laoding,0-2两种方式

	    	  
	    	  demoText.html('<span class="layui-inline layui-upload-choose">'+file.name+'</span>');
	      });
	    } 
	    
	 
	    //,multiple: true
	   // ,bindAction: '#${id!}_button_begin'
	    ,done: function(res){
	    	if(file_loadindex != 0)
	    		layer.close(file_loadindex);    
	      //如果文件上传失败
	      if(res.success == false){
	    	  $('#${id!}').attr('value', ""); 
	        return layer.msg('上传失败:'+res.msg);
	      }
	      //上传成功
	      $('#${id!}').attr('value', res.fileName); 
	      
	      var demoText = $('#${id!}_text');
	      var fileName = demoText.text();
	      
	      showFile_${id!}(res.fileName,fileName);
	      //添加一个隐藏的输入框，设置文件名字段值：
	      demoText.append('<input type="hidden"  id="${fileNameKey!}" name="${fileNameKey!}"  value="'+fileName+'" />');
	      
	      
	    }
	    ,error: function(){
	      //演示失败状态，并实现重传
	      var demoText = $('#${id!}_text');
	      demoText.html('<span style="color: #FF5722;">上传失败</span> <a class="layui-btn layui-btn-mini file-reload_${id!}">重试</a>');
	      demoText.find('.file-reload_${id!}').on('click', function(){
	        uploadInst_${id!}.upload();
	      });
	    }
	  });
});

//path 为局部地址如：/upload/aaa.jpg,name=/upload/aaa.jpg或者 aaa.jpg即可
function showFile_${id!}(path,name){
	  var demoText = $('#${id!}_text');
	  var url = '';
	  if(path != ''){
		  if(path.indexOf("http://") == -1){
			  
			  url = '${FILE!}'+'/' +path;
		  }else
			  url = path;
	  }
	  

	  
	  //文件名需要 ‘/111/a.jpg’=》裁剪成 a.jpg （如果出现的话）
	  var index = name.lastIndexOf('/');
	  if(index != -1)
		  name = name.substring(index+1);
	  
      demoText.html('<a href="'+url+'" target="view_window" download="'+name+'"><span class="layui-inline layui-upload-choose" >'+name+'</span></a>');
}

</script>