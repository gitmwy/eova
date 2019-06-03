<%
// data-options


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



if( isTrue(isReadonly!) || isTrue(view!)  ){
	verify ='';
}


%>



<!--style给定宽度可以影响编辑器的最终宽度 https://www.cnblogs.com/iLoveMyD/p/4606330.html image.js被修改 var r = this.contentWindow.document.body.innerHTML;
style="width:100%;height:340px;"
-->
<script type="text/plain" id="editor_${id!}" >
<p>${value!}</p>
</script>
<!-- 隐藏值 -->
<input type="hidden"  id="${id!}" name="${name!}" title='${item.cn!}'  lay-verify="${verify!}"/>
<script type="text/javascript">
$(function () {
    //实例化编辑器width="${width}" height
    var um = UM.getEditor('editor_${id!}',
    		     {
    		         imageUrl:"${STATIC!}/upload/editor?filedir=/image", //处理图片上传的接口
    		         imagePath:"", //路径前缀
    		         imageFieldName:"upfile", //上传图片的表单的name
    		         initialFrameHeight:'${height!}',initialFrameWidth:'${width!}' 
    		     });
    
    $('#${id!}').attr('value', '${value!}'); 
    
    //setDisabled();
    um.addListener('blur',function(){//编辑器失去焦点了正好把数据放入 
       // $('#focush2').html('编辑器失去焦点了');
        //console.log('blur:'+um.getContent());
        $('#${id!}').attr('value', um.getContent()); 
    });
    um.addListener('focus',function(){
        $('#focush2').html('');
        
        console.log('focus:'+um.getContent());
    });
    
    <% if( isTrue(isReadonly!) || isTrue(view!)  ){ %>
    setDisabled();
    <% }%>
    //按钮的操作
    function insertHtml() {
        var value = prompt('插入html代码', '');
        um.execCommand('insertHtml', value)
    }
    function isFocus(){
        alert(um.isFocus())
    }
    function doBlur(){
        um.blur()
    }
    function createEditor() {
        enableBtn();
        um = UM.getEditor('myEditor');
    }
    function getAllHtml() {
        alert(UM.getEditor('myEditor').getAllHtml())
    }
    function getContent() {
        var arr = [];
        arr.push("使用editor.getContent()方法可以获得编辑器的内容");
        arr.push("内容为：");
        arr.push(UM.getEditor('myEditor').getContent());
        alert(arr.join("\n"));
    }
    function getPlainTxt() {
        var arr = [];
        arr.push("使用editor.getPlainTxt()方法可以获得编辑器的带格式的纯文本内容");
        arr.push("内容为：");
        arr.push(UM.getEditor('myEditor').getPlainTxt());
        alert(arr.join('\n'))
    }
    function setContent(isAppendTo) {
        var arr = [];
        arr.push("使用editor.setContent('欢迎使用umeditor')方法可以设置编辑器的内容");
        UM.getEditor('myEditor').setContent('欢迎使用umeditor', isAppendTo);
        alert(arr.join("\n"));
    }
    //禁止编辑（只读模式）
    function setDisabled() {
    	um.setDisabled('fullscreen');
       // disableBtn("enable");
    }

    function setEnabled() {
        UM.getEditor('myEditor').setEnabled();
        enableBtn();
    }

    function getText() {
        //当你点击按钮时编辑区域已经失去了焦点，如果直接用getText将不会得到内容，所以要在选回来，然后取得内容
        var range = UM.getEditor('myEditor').selection.getRange();
        range.select();
        var txt = UM.getEditor('myEditor').selection.getText();
        alert(txt)
    }

    function getContentTxt() {
        var arr = [];
        arr.push("使用editor.getContentTxt()方法可以获得编辑器的纯文本内容");
        arr.push("编辑器的纯文本内容为：");
        arr.push(UM.getEditor('myEditor').getContentTxt());
        alert(arr.join("\n"));
    }
    function hasContent() {
        var arr = [];
        arr.push("使用editor.hasContents()方法判断编辑器里是否有内容");
        arr.push("判断结果为：");
        arr.push(UM.getEditor('myEditor').hasContents());
        alert(arr.join("\n"));
    }
    function setFocus() {
        UM.getEditor('myEditor').focus();
    }
    function deleteEditor() {
        disableBtn();
        UM.getEditor('myEditor').destroy();
    }
    function disableBtn(str) {
        var div = document.getElementById('btns');
        //var btns = domUtils.getElementsByTagName(div, "button");
       
    }
    function enableBtn() {
        var div = document.getElementById('btns');
        var btns = domUtils.getElementsByTagName(div, "button");
        for (var i = 0, btn; btn = btns[i++];) {
            domUtils.removeAttributes(btn, ["disabled"]);
        }
    }
});
</script>