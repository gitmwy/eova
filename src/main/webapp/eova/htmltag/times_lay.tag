<% //查询用 >=start_${name} 小于end_${name} %>
<div class="layui-input-inline"  title="大与等于">
	<input type="text" class="layui-input" id="start_${id}" name="start_${name}" style="width: 100px;"  placeholder="${start_placeholder!}">
</div>
<div class="layui-input-inline">-</div>
<div class="layui-input-inline"  title="小于">
	<input type="text" class="layui-input" id="end_${id}" name="end_${name}" style="width: 100px;" placeholder="${end_placeholder!}">
</div>
<!-- 可以通过js设置此值，设置区间 -->
<input type="hidden" id="max_${id}" name="max_${name}" value='7'>



<script>

layui.use('laydate', function(){
  var laydate = layui.laydate;
  
  
  
  //日期时间选择器(目前只支持 yyyy-MM-dd 吧)
  var start = laydate.render({
    	elem: '#start_${id}'
    	,type: 'date'
    		  ,isInitValue: true //允许填充初始值
    		  ,calendar: true
    		  //,max: '2018-07-07'
    		 ,done: function (value, date) {  //选完触发
    			 var maxday =  $("#max_${id}").val()*1;  
    			 console.log(maxday);
    			 end.config.min={  
    				        year:date.year,   
    				        month:date.month-1,  
    				        date: date.date
    				      }  
    			 end.config.max={  
 				        year:date.year,   
 				        month:date.month-1,  
 				        date: date.date+maxday
 				      } 
    			 
    			 console.log(date.date);
    			 console.log(date.date+maxday);
    			}

  });
  var end =  laydate.render({
	   			 elem: '#end_${id}'
	    		,type: 'date'
	    		  ,isInitValue: true //允许填充初始值
	    		  ,calendar: true
	    		  ,done: function (value, date) {  //选完触发
	    			  var maxday =  $("#max_${id}").val()*1;  
	    			  start.config.max={  
	     				        year:date.year,   
	     				        month:date.month-1,  
	     				        date: date.date
	     				      } 
	    			  
	    			  start.config.min={  
	     				        year:date.year,   
	     				        month:date.month-1,  
	     				        date: date.date - maxday
	     				      }  
	     			}
	  });
  
  
});
</script>