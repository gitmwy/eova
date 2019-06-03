<form id="${id}" name="${id}" method="post"  class="layui-form layui-form-pane">
<%
		// Form Widget: 。
		// 下面的代码，必须非常熟悉Beetl再看如下代码。
		// 用到Beetl各种常规语法+自定义函数+Tag
		//只是查看模式view，所有字段都是只读并且按钮为“确认”
		
		
		// 获取分组元字段数据
			// 获取分组元字段数据（友查询改成 后台直接可以带出来）
		//var fields = query("fields", objectCode); 
		var fields =object.fields; 
		
		var lastFieldset = "";
		var isFirstEnd = false;
		
		for(f in fields){
		
			// 如果当前分组名和上一个分组名不一样，输出分组标签
			if(f.fieldset != lastFieldset){
				
				// 第一个分组结束闭合标签
				if(isFirstEnd){
					print('</fieldset>');
				}
				
				isFirstEnd = true;				// 分组标签待闭合标记
				lastFieldset = f.fieldset;		// 记录当前分组名
				
				print('<fieldset class="layui-elem-field">');
				print('<legend>' + f.fieldset + '</legend>');
			}

			if(!isEmpty(data!)){
				// 有数据为更新模式
				if(f.update_status < 50){		 	// 未禁用

					var value = @data.get(f.en);	// 获取当前字段的值
					var isReadOnly = false;		// 当前字段是否只读
					if(f.update_status == 10){
						isReadOnly = true;
					}
					
					// 获取固定值(必须是JSON)
					if(!isEmpty(fixed)){
						// JSON对象化
						var fix = parseJson(fixed);
						var fixedValue = fix[f.en];
						// 固定值覆盖动态值并且只读
						if(!isEmpty(fixedValue)){
							value = fixedValue;
							isReadOnly = true;
						}
					}
					
					
					
					
					if(f.update_status == 20){		// 隐藏字段(时间字段没格式化，建议不要带出来修改了)
						%>
						<#field_hidden item="${f}" fixed="${fixed!}" value="${value!f.defaulter}" />
						<% 
					}else{
						%>
						<div class="layui-form-item " >
						    <label class="layui-form-label" title="${f.cn}[${f.en}]">
						    <% if(isTrue(f.is_required!)){ %><font size="4" face="arial" color="red">*</font><% } %>
						    ${f.cn}</label>
						    <div class="layui-input-block" >
						      	  <#field_lay item="${f}" value="${value!f.defaulter}" readnoly="${isReadOnly}" view="${view!}"/>
						    </div>
<!-- 						    <div class="layui-form-mid layui-word-aux">请务必填写用户名</div> -->
						  </div>
						<%
					}
				}
			} else {
				// 无数据为新增模式
				if(f.add_status < 50){			// 未禁用
					var value = null;
					var isReadOnly = false;		// 当前字段是否只读
					if(f.add_status == 10){
						isReadOnly = true;
					}
					
					// 获取固定值(必须是JSON)
					if(!isEmpty(fixed)){
						// JSON对象化
						var fix = parseJson(fixed);
						var fixedValue = fix[f.en];
						// 固定值覆盖动态值并且只读
						if(!isEmpty(fixedValue)){
							value = fixedValue;
							isReadOnly = true;
						}
					}
					
					
					
					if(f.add_status == 20){		// 隐藏字段 class="layui-hide"
						%>
					<#field_hidden item="${f}" fixed="${fixed!}" value="${value!f.defaulter}" />
						<% 					
					}else{
						%>
						
						
						  <div class="layui-form-item " >
						    <label class="layui-form-label" title="${f.cn}[${f.en}]">
						    <% if(isTrue(f.is_required!)){ %><font size="4" face="arial" color="red">*</font><% } %>
						    ${f.cn}</label>
						    <div class="layui-input-block" >
						      	  <#field_lay item="${f}" value="${value!f.defaulter}" readnoly="${isReadOnly}" view="${view!}"/>

						    </div>
<!-- 						    <div class="layui-form-mid layui-word-aux">请务必填写用户名</div> -->
						  </div>
						  
						  
						
						<%
					}
				}
			}
	
			// 最后一个分组进行闭合
			if(fLP.last && !isEmpty(f.fieldset!)){
				%></fieldset><%
			}
			
		}
%>
<div class="layui-form-item">
    <div class="layui-input-block">
      <button class="layui-btn" lay-submit="" lay-filter="submit">
      <%if(!isTrue(view!)){ %>
    	  立即提交
      <% }else{ %>确认<% } %>
      </button>
<!--       <button type="reset" class="layui-btn layui-btn-primary">重置</button> -->
								<%// 仅超级管理员可见 %>
        						<%if(session.user.isAdmin){%>
        						<#design_button_lay objectCode="${object.code}" formType="${VIEW_FORM!(PAGE_TYPE+'Form')}" />
								<%}%>
    </div>
  </div>

</form>
<script>


layui.use(['form','code'], function(){
	  var form = layui.form; //只有执行了这一步，部分表单元素才会自动修饰成功
	   $ = layui.$;
	   
       // 代码块
       layui.code({
           title: 'html',
           encode: true,
           about: false

       });
	
       // 手动赋值
       //$('select[name="简化多选+搜索+大小写敏感"]').val(['sing1', 'movie2']);
       form.render();
	  
	  //但是，如果你的HTML是动态生成的，自动渲染就会失效
	  //因此你需要在相应的地方，执行下述方法来手动渲染，跟这类似的还有 element.init();
       form.on('submit(submit)', function(data){
    		  //console.log(data.elem) //被执行事件的元素DOM对象，一般为button对象
    		  //console.log(data.form) //被执行提交的form对象，一般在存在form标签时才会返回
    		  //console.log(data.field) //当前容器的全部表单字段，名值对形式：{name: value}
    		  //console.log(syntaxHighlight(data.field));
    		  
    		  //提交数据
    		  submitNow(data);
    		  return false; //阻止表单跳转。如果需要表单跳转，去掉这段即可。
    		});
	  
	}); 

// json 格式化+高亮
function syntaxHighlight(json) {
    if (typeof json != 'string') {
        json = JSON.stringify(json, undefined, 2);
    }
    json = json.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function(match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}
</script>