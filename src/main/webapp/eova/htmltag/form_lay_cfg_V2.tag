<form id="${id}" name="${id}" method="post"  class="layui-form">
<div class="layui-fluid">
   <div class="layui-row layui-col-space15">
     <div class="layui-col-md12">

<%
		// Form Widget: 。
		// 下面的代码，必须非常熟悉Beetl再看如下代码。
		// 用到Beetl各种常规语法+自定义函数+Tag
		
		//2中模式，第一无配置信息的情况 ，第二有配置新版数据的情况（拖拽形成的配置）结构如下
		/* {
"addForm": {
"force": 2,
"list": [
  {
    "name": "请输入分组名",
    "order": 1,
    "datas": [
      {
        "line": 1,
        "data": [
          {
            "width": "12",
            "name": "名称",
            "explain": "名称[name]",
            "id": "name",
            "defaultVal": "",
            "ismust": false
          }
        ]
      },
      {
        "line": 2,
        "data": [
          {
            "width": "6",
            "name": "班级号",
            "explain": "班级号[class_no]",
            "id": "class_no",
            "defaultVal": "",
            "ismust": false
          },
          {
            "width": "6",
            "name": "状态",
            "explain": "状态[status]",
            "id": "status",
            "defaultVal": "请选择",
            "ismust": false
          }
        ]
      }
    ]
  },
  {
    "name": "隐藏域",
    "order": 0,
    "datas": [
      {
        "line": 1,
        "data": [
          {
            "id": "id",
            "defaultVal": "",
            "placeholder": "",
            "verify": "",
            "ismust": true
          }
        ]
      },
      {
        "line": 2,
        "data": [
          {
            "id": "create_user",
            "defaultVal": "${user.id}",
            "verify": "",
            "ismust": false
          }
        ]
      }
    ]
  }
]
} */
		
		var config = null;//from 的配置信息，比如一行几个 都有谁，如果不存在则使用目前老的模式，分组，顺序铺
		var formViews = null;
		var force = 0;

		var formtype = type;
		
		//是否要强制用view来显示，而不取决数据的是否隐藏
		if(!isEmpty(object.config!)){
			//formView = object.config[formtype];
			config = object.config.config;
			if(!isEmpty(VIEW_FORM!)){
				formViews = config[VIEW_FORM];
			}else{
				formViews = config[formtype];
				if(formViews == null){
					formViews = config.form;
				}
			}
		}

	
		
		formViews = formViews.list!;
		// 获取分组元字段数据（友查询改成 后台直接可以带出来）
		//var fields = query("fields", objectCode); 
		var fields = object.fields;
		
		//先输出隐藏域
		
		


		
		
		//注意隐藏的如果没配置也需要输出出来
	
		
		
		var formViewsLen = formViews.~size;
		
		var x = 0;
		
		var hiddenGroup = null;
		if(formViewsLen>0)	{
			if(formViews[formViewsLen-1].order == 0){
				hiddenGroup = formViews[formViewsLen-1];
				
				formViewsLen = formViewsLen-1;
			}
				
		}
		
		 
		for(var i=0;i<formViewsLen;i++){
			var g = formViews[i];
			var order = g.order;//如果是0 则是隐藏组，不需要输出
			var groupName = g.name;
			var datas = g.datas;
			%>
		
		
			
			<div class="layui-card">
			<div class="layui-card-header">${groupName!}</div>
			<div class="layui-card-body">
			
			<%
			for(oneline in datas){
				var lineNum = oneline.line;
				var lineDatas = oneline.data;
				
				//输出line字段  开始	
				%>
					<div class="layui-row layui-col-space10 layui-form-item">
					<%
					
					var col ;
					for(onefield in lineDatas){ 
						var outlabel = true;
						var fieldid = onefield.id;
						var isReadOnly = onefield.readOnly;
						var defaultVal = strutil.out(onefield.defaultVal,null);
						var placeholder = strutil.out(onefield.placeholder,null);
						var ismust = onefield.ismust;
						var verify = strutil.out(onefield.verify,null);
						var limit = strutil.out(onefield.limit,null);
						
						if(isEmpty(fieldid!))
							continue;
						
						
						
						if(isEmpty(onefield.name!))
							outlabel = false;
						
						var f = null;
						for(ftemp in fields){
							if(ftemp.en == fieldid){//输出
								f = ftemp;
							
								break;
							}
						}
						///debug(f);//添加字段，从配置json=》f ,set是之前有值，put是直接放入
						if(!isEmpty(verify!))
							@f.set('validator',verify);
						@f.set('is_required',ismust);
						if(!isEmpty(limit!))
							@f.put('limit',limit);
						
						var value = null;
						if(data != null)
							value = @data.get(f.en);// 获取当前字段的值
							
						
						// 获取固定值(必须是JSON)
						if(!isEmpty(fixed!)){
							// JSON对象化
							var fix = parseJson(fixed);
							
							var fixedValue = fix[f.en];
							// 固定值覆盖动态值并且只读
							if(!isEmpty(fixedValue)){
								value = fixedValue;
								isReadOnly = true;
							}
						}
					%>	
						
						<div class="layui-col-lg${onefield.width!}">
						<% if(outlabel){ %>
									<label class="layui-form-label" title="${onefield.explain!}">
									<% if(isTrue(onefield.ismust!)){ %><font size="4" face="arial" color="red">*</font><% } %>
									${onefield.name!}</label>
									
									<div class="layui-input-block" >
						<% } %>			
										
											 <#field_lay item="${f}" placeholder="${placeholder!}" value="${value!onefield.defaultVal!}" readnoly="${isReadOnly}" />
					     <% if(outlabel){ %>
					                  </div>
					     <% } %>	
					   </div>
						
					<%	
					}
					%>
					</div>
				<%//输出line字段 结束
			}
			%>
			
			
		
			
			<%
			//判断是否最后一组(输出隐藏字段以及按钮)
			if( i == formViewsLen-1){//最后一组
				%>
			 <div class="layui-form-item">
                <div class="layui-input-block">
                  <button class="layui-btn" lay-submit="" lay-filter="submit">
			      <%if(!isTrue(view!)){ %>
			    	  立即提交
			      <% }else{ %>确认<% } %>
			      </button>
			      
			      <%// 仅超级管理员可见 %>
        		  <%if(session.user.isAdmin){%>
        						<#design_button_lay objectCode="${object.code}" formType="${VIEW_FORM!(PAGE_TYPE+'Form')}" />
				  <%}%>
                </div>
             </div>
             <%} 
             %>
			
			
			</div>
			</div>

			
		<% }
		if(hiddenGroup!= null){
			//debug(hiddenGroup);
			
			for(oneline in hiddenGroup.datas){
			
				for(onefield in oneline.data){
					////debug(onefield);
					var fieldid = onefield.id;
					var isReadOnly = onefield.readOnly;
					var defaultVal = strutil.out(onefield.defaultVal,null);
					var placeholder = strutil.out(onefield.placeholder,null);
					var ismust = onefield.ismust;
					var verify = strutil.out(onefield.verify,null);
					
					if(isEmpty(fieldid))
						continue;
					//if(!isEmpty(verify!))
					//	@f.set('validator',validator);
					//@f.set('is_required',ismust);
					
					//debug(fieldid);
			
					var f = null;
					for(ftemp in fields){
						if(ftemp.en == fieldid){//输出
							f= ftemp;
						
							break;
						}
					}
					var value = null;
					if(data != null)
						value = @data.get(f.en);// 获取当前字段的值
					//debug(f);
					// 获取固定值(必须是JSON)
					if(!isEmpty(fixed!)){
						// JSON对象化
						var fix = parseJson(fixed!);
						var fixedValue = fix[f.en];
						// 固定值覆盖动态值并且只读
						if(!isEmpty(fixedValue)){
							value = fixedValue;
							isReadOnly = true;
						}
					}
	
						
						%>
						<#field_hidden item="${f}" fixed="${fixed!}" value="${value!onefield.defaultVal!}" />
						<%
					}
				
			}
		}
		%>



 	</div>
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