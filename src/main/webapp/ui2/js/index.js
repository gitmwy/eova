var $,tab,dataStr,layer,template;
layui.config({
	base : "../ui2/js/"
}).extend({
	"bodyTab" : "bodyTab"
})
layui.use(['bodyTab','form','element','layer','jquery'],function(){
	var form = layui.form,
		element = layui.element;
		$ = layui.$;
    	layer = parent.layer === undefined ? layui.layer : top.layer;
		tab = layui.bodyTab({
			openTabNum : "50",  //最大可打开窗口数量
			url : "../showTree" //获取菜单json地址 ../showTree?pId=3
		});

	//通过顶部菜单获取左侧二三级菜单   注：此处只做演示之用，实际开发中通过接口传参的方式获取导航数据（如果顶部菜单未初始，则初始化）
	function getData(nodeId,nodeName){
		$.getJSON(tab.tabConfig.url+'?pId='+nodeId,function(data){
			dataStr = data;
			//模板
			if( nodeName == '平台维护'){//只有平台维护不加，
				template='';
			}else
				template='lay';
			
			
			
			//重新渲染左侧菜单
			tab.render();
			
			
		})
	}
	//页面加载时判断左侧菜单是否显示
	//通过顶部菜单获取左侧菜单
	$(".topLevelMenus li,.mobileTopLevelMenus dd").click(function(){
		if($(this).parents(".mobileTopLevelMenus").length != "0"){
			$(".topLevelMenus li").eq($(this).index()).addClass("layui-this").siblings().removeClass("layui-this");
		}else{
			$(".mobileTopLevelMenus dd").eq($(this).index()).addClass("layui-this").siblings().removeClass("layui-this");
		}
		$(".layui-layout-admin").removeClass("showMenu");
		$("body").addClass("site-mobile");
		getData($(this).data("menu"),$(this).data("name"));
		//渲染顶部窗口
		tab.tabMove();
	})

	//隐藏左侧导航
	$(".hideMenu").click(function(){
		if($(".topLevelMenus li.layui-this a").data("url")){
			layer.msg("此栏目状态下左侧菜单不可展开");  //主要为了避免左侧显示的内容与顶部菜单不匹配
			return false;
		}
		$(".layui-layout-admin").toggleClass("showMenu");
		$(".layui-layout-admin-hideMenu").toggleClass("layui-icon-shrink-right");
		$(".layui-layout-admin-hideMenu").toggleClass("layui-icon-spread-left");
		
		//渲染顶部窗口
		tab.tabMove();
	})
	
	//data-menu
	//alert($(".layui-this")[1].text());
	//console.log($($(".layui-this")[1]).data("name"));
	//通过顶部菜单获取左侧二三级菜单   注：此处只做演示之用，实际开发中通过接口传参的方式获取导航数据
	getData($($(".layui-this")[1]).data("menu"),$($(".layui-this")[1]).data("name"));

	//手机设备的简单适配
    $('.site-tree-mobile').on('click', function(){
		$('body').addClass('site-mobile');
	});
    $('.site-mobile-shade').on('click', function(){
		$('body').removeClass('site-mobile');
	});

	// 添加新窗口
	$("body").on("click",".layui-nav .layui-nav-item a:not('.mobileTopLevelMenus .layui-nav-item a')",function(){
		//如果不存在子级
		if($(this).siblings().length == 0){
			addTab($(this));
			$('body').removeClass('site-mobile');  //移动端点击菜单关闭菜单层
		}
		$(this).parent("li").siblings().removeClass("layui-nav-itemed");
	})

	//清除缓存
	$(".clearCache").click(function(){
		window.sessionStorage.clear();
        window.localStorage.clear();
        var index = layer.msg('清除缓存中，请稍候',{icon: 16,time:false,shade:0.8});
        setTimeout(function(){
            layer.close(index);
            layer.msg("缓存清除成功！");
        },1000);
    })

	//刷新后还原打开的窗口
    if(cacheStr == "true") {
        if (window.sessionStorage.getItem("menu") != null) {
            menu = JSON.parse(window.sessionStorage.getItem("menu"));
            curmenu = window.sessionStorage.getItem("curmenu");
            var openTitle = '';
            for (var i = 0; i < menu.length; i++) {
                openTitle = '';
                if (menu[i].icon) {
                    if (menu[i].icon.split("-")[0] == 'icon') {
                        openTitle += '<i class="seraph ' + menu[i].icon + '"></i>';
                    } else {
                        openTitle += '<i class="layui-icon">' + menu[i].icon + '</i>';
                    }
                }
                openTitle += '<cite>' + menu[i].title + '</cite>';
                openTitle += '<i class="layui-icon layui-unselect layui-tab-close" data-id="' + menu[i].layId + '">&#x1006;</i>';
                element.tabAdd("bodyTab", {
                    title: openTitle,
                    content: "<iframe src='" + menu[i].href + "' data-id='" + menu[i].layId + "'></frame>",
                    id: menu[i].layId
                })
                //定位到刷新前的窗口
                if (curmenu != "undefined") {
                    if (curmenu == '' || curmenu == "null") {  //定位到后台首页
                        element.tabChange("bodyTab", '');
                    } else if (JSON.parse(curmenu).title == menu[i].title) {  //定位到刷新前的页面
                        element.tabChange("bodyTab", menu[i].layId);
                    }
                } else {
                    element.tabChange("bodyTab", menu[menu.length - 1].layId);
                }
            }
            //渲染顶部窗口
            tab.tabMove();
        }
    }else{
		window.sessionStorage.removeItem("menu");
		window.sessionStorage.removeItem("curmenu");
	}
})

//打开新窗口
function addTab(_this){
	tab.tabAdd(_this);
}

//手工添加tab
function addTabManual(name,icon,url,_default){
	//设置隐藏的菜单值，然后调用tabAdd
	//$('.myhidden-nav-item')
	//$('.myhidden-nav-item').attr("data-url") = url;
	
	$('.myhidden-nav-item').append('<a data-url="'+url+'" class="layui-nav-item"><cite><i class="layui-icon">'+icon+'</i>'+name+'</cite></a>');　　　　
	var a = $('.myhidden-nav-item').children();
	addTab(a);
	
	//完成以后移除数据
	$('.myhidden-nav-item').empty();
}


//捐赠弹窗
function donation(){
	layer.tab({
		area : ['260px', '367px'],
		tab : [{
			title : "微信",
			content : "<div style='padding:30px;overflow:hidden;background:#d2d0d0;'><img src='images/wechat.jpg'></div>"
		},{
			title : "支付宝",
			content : "<div style='padding:30px;overflow:hidden;background:#d2d0d0;'><img src='images/alipay.jpg'></div>"
		}]
	})
}

//图片管理弹窗
function showImg(){
    $.getJSON('json/images.json', function(json){
        var res = json;
        layer.photos({
            photos: res,
            anim: 5
        });
    });
}