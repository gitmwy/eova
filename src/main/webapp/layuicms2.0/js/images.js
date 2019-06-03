layui.config({
	base : "../../js/"
}).use(['flow','form','layer','upload'],function(){
    var flow = layui.flow,
        form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        upload = layui.upload,
        $ = layui.jquery;

    //流加载图片
    var imgNums = 15;  //单页显示图片数量
    flow.load({
        elem: '#Images', //流加载容器
        done: function(page, next){ //加载下一页
            $.get("/layuicms2.0/json/images.json",function(res){
            	res =	eval('(' + res + ')');
            	
                //模拟插入
                var imgList = [],data = res.data;
                var maxPage = imgNums*page < data.length ? imgNums*page : data.length;
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
                }, 200);
            });
        }
    });

    //设置图片的高度
    $(window).resize(function(){
        $("#Images li img").height($("#Images li img").width());
    })

  

})