<form id="${id}" name="${id}" method="post"  class="layui-form">
<div class="layui-fluid">
   <div class="layui-row layui-col-space15">
     <div class="layui-col-md12">

<%
		// Form Widget: 。
		// 下面的代码，必须非常熟悉Beetl再看如下代码。
		// 用到Beetl各种常规语法+自定义函数+Tag
		//只是查看模式view，所有字段都是只读并且按钮为“确认”
		
		//2中模式，第一无配置信息的情况 ，第二有配置数据的情况，（如果没有配置信息则走第一种默认情况）
		//object.config配置信息如下，form格式如下，如果存在此配置，可以存在addForm 以及 updateForm，以及 detailFrom 以及 form ，前面优先，如果都不存在则走默认模式（默认模式不搞什么分组了）
		//主要说明下force 0/1区别
		// 0-只输出view中标记 且能输出的（正常的），最后输出隐藏的，1-输出view中标记,最后输出那些隐藏且未输出过的
		//剩余字段，直接隐藏输出（VIEW_FORCE=false则隐藏的，VIEW_FORCE=true则未输的）
		/* {
  "form": [
    {
      "name": "面板1",
      "order": 1,
      "datas": [
        {
          "line": "1",
          "data": "name,tag1,tag2"
        },
        {
          "line": "2",
          "data": "password,relation_pro"
        }
      ]
    },
    {
      "name": "面板2",
      "order": 1,
      "datas": [
        {
          "line": "1",
          "data": "status"
        },
        {
          "line": "2",
          "data": "avatar,file"
        }
      ]
    }
  ]
} */
		
		var config = null;//from 的配置信息，比如一行几个 都有谁，如果不存在则使用目前老的模式，分组，顺序铺
		var formView = null;
		var force = 0;

		var formtype = type;
		
		//是否要强制用view来显示，而不取决数据的是否隐藏
		
		if(!isEmpty(object.config!)){
			//formView = object.config[formtype];
			config = object.config.config;
			if(!isEmpty(VIEW_FORM!)){
				formView = config[VIEW_FORM];
			}else{
				formView = config[formtype];
				if(formView == null){
					formView = config.form;
				}
			}
		}
		//formView
		debug(formView);
		if(!isEmpty(formView.force!)){
			force = formView.force!;
		}
		formView = formView.list!;
		// 获取分组元字段数据（友查询改成 后台直接可以带出来）
		//var fields = query("fields", objectCode); 
		var fields = object.fields;
		
		//先输出隐藏域
		
		
		
		debug(force);
		
		var lastFieldset = "";
		var isFirstEnd = false;
		
		//需要检测下是否有分组（分组号是否唯一）
		var exitfield = false;
		var lastFieldnum =-1;
	
		var allFieldsStr = null;
		
		
		//注意隐藏的如果没配置也需要输出出来
		lastFieldnum = -1;
		
		
		
		for(g in formView){


			var groupName = g.name;
			var datas = g.datas;
		
			println('<div class="layui-card">');
			println('<div class="layui-card-header">'+groupName+'</div>');
			println('<div class="layui-card-body">');
			
			
			
			
			for(oneline in datas){
				//需要输出的属性 ，添加元素： @linelist.add(6);
				 var linelist = [];
				
				if(!isEmpty(allFieldsStr!))
					allFieldsStr = allFieldsStr+","+oneline.data!;
				else
					allFieldsStr = oneline.data!+'';
				
				var linedatas = strutil.split(oneline.data!+'', ',');
				
				var tt = 12;
				var length = linedatas.~size;//计算份额
				
				//println('<div class="layui-row layui-col-space10 layui-form-item">');
				
				if(length != 0){//如果不为0，为0就暂时认定为换号
					//需要判断分成几块(2种情况，)
					var remainder =  tt%length;
					
					
					//需要判断分成几块(2种情况，1种 'avatar(2),file(2)',1种 'avatar,file')=>avatar(-2),file(2) ,'-'表示不要输出字段名直接输出值
					var col = (tt-remainder)/length;
					
					
					for(oneKey in linedatas){
						if( strutil.index(oneKey,'(') != -1 ){
							col = strutil.subStringTo(oneKey,strutil.index(oneKey,'(')+1,strutil.index(oneKey,')'));
							oneKey = strutil.subStringTo(oneKey,0,strutil.index(oneKey,'('));
						}
						
						for(f in fields){
							if(f.en == oneKey){//输出
								//如果是隐藏的域直接跳出，需前面或者尾部集中输出隐藏的域（因为用户可能隐藏域未配置在form中）
							
								if(force == 0 && (
										(!isEmpty(data!) && (f.update_status == 20 || f.update_status == 50))
										||(isEmpty(data!) && (f.add_status == 20 || f.add_status == 50))
										)
										){
									break;
								}
								
							@f.put("mycol",col);
								@linelist.add(f);
				                break;
							}
						}	
					}
				}
				
				
				//println('</div>');
				//输出line字段  开始	
				%>
					<div class="layui-row layui-col-space10 layui-form-item">
					<%
					
					var col ;
					for(f in linelist){ 
						col = parseInt(@f.get("mycol"));	;
						
						var outlabel =true;
						debug(col);
						if(col<0){
							outlabel = false;
							col = 0- col;
						}
					%>	
						
						<div class="layui-col-lg${col!}">
						<% if(outlabel){ %>
									<label class="layui-form-label" title="${f.cn}[${f.en}]">
									<% if(isTrue(f.is_required!)){ %><font size="4" face="arial" color="red">*</font><% } %>
									${f.cn!}</label>
									
									<div class="layui-input-block" >
						<% } %>			
										
											<#form_field_out_lay f="${f}" fixed="${fixed!}" data="${data!}"  view="${view!}" force="${force!}"/>
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
			//判断是否最后一组（最后一组都已经输出完成， 是否还有下脚料需要处理）
			//1、force=0情况下，正常输出剩下未标记的（除了隐藏的） 2、force=1的情况 不输出                  目前暂定这样处理，看后面需求
			//，然后输出按钮(还有一个判断，如果字段未设置位置， 则直接输出到最后 提醒用户 一行一个？)
			//debug(gLP.index);
			//debug(formView.~size);
			
			if( gLP.index == formView.~size){
				
				//判断是否有没有输出的
				//debug(allFieldsStr);
				var allFields = strutil.split(allFieldsStr, ',');
				for(f in fields){
					if(force == 1){
						continue;
					}else if(force == 0 && (
							(!isEmpty(data!) && (f.update_status == 20 || f.update_status == 50))
							||(isEmpty(data!) && (f.add_status == 20 || f.add_status == 50))
							)
							){
						continue;
					}else{
						
						if((isEmpty(data!) && f.add_status < 50)
							||	!isEmpty(data!) && f.update_status < 50
								){//新增|更新,需要输出的，检测下是否输出了，如果没有则要输出
							
							var isOut = false;

							for(fStr in allFields){
								if( strutil.index(fStr,'(') != -1 ){
									fStr = strutil.subStringTo(fStr,0,strutil.index(fStr,'('));
								}
								if(fStr == f.en){
									isOut = true;
									
									break;
								}
							}
							
							if(!isOut){
								%>
								<div class="layui-row layui-col-space10 layui-form-item">
									<div class="layui-col-lg12}">
									<label class="layui-form-label">${f.cn!}</label>
										<div class="layui-input-block">
										<#form_field_out_lay f="${f}" fixed="${fixed!}" data="${data!}"  view="${view!}" force="${force!}"/>
					                    </div>
					                </div>
								</div>
								<%
							}
						}
						
					}
				}
				//if(){
				%>
					
			<%	//}
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

			
		<%
		
		}
		for(f in fields){
			//输出隐藏域，除非前面已经输出过(&& !isEmpty(realData!) 暂时被屏蔽，如果真的不需要输出空值则在field_hidden。tag处理下)
			var realData = value!f.defaulter!;
			if((!isEmpty(data!) && f.update_status == 20)
					||(isEmpty(data!)  && f.add_status == 20)
					){
				var value = null;
				if(!isEmpty(data!)){
					// 有数据为更新模式
					value = @data.get(f.en);
				}
				//如果force = 0直接输出，否则判断是否输出过否则才输出
						
				var allFields = strutil.split(allFieldsStr, ',');
				var needout = false;
				if(force == 1){
					needout = true;
					for(fStr in allFields){
						if( strutil.index(fStr,'(') != -1 ){
							fStr = strutil.subStringTo(fStr,0,strutil.index(fStr,'('));
						}
						if(fStr == f.en){
							needout = false;
							break;
						}
					}
					
				}
				
				
				if(force == 0 || needout){//0模式或者1模式下未输出
				%>
				<#field_hidden item="${f}" fixed="${fixed!}" value="${value!f.defaulter}" />
				<%}
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