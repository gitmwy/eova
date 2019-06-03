//数据加载完成
function dataDone(res, curr, count,is_single){
	var index ;
		        $("img.small_img").hover(function() {
		                var img = "<img style='width: 100%;' src='"+$(this).attr('src')+"'/>";
		                index = layer.tips(img, this,{
		                	tips:[2, 'rgba(41,41,41,0.5)'],
		                	area: ['250px'],
		                    time: 200000
		                });
		        },function() {
					layer.close(index);
		        });
}
