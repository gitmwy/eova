# 积木(building block 简称BB)


#### 项目介绍
像积木一样搭建管理平台，修改自EOVA前端调整成layui(后端模板layuicms)。
Eova采用的是引擎模式 支持单表、一对多表等。
设计分为 控件、组件、业务三层，组件由控件组装成，业务由组件构成，每层均可自由定制。理论上每种控件，每种组件，每种业务只实现一次，后续直接复用。
（结尾附软件操作视频）

#### 项目运行
1. git同步至eclipse（普通项目）
2. 然后项目右键 Properties=>Project Facets 选中java即可
3. 设置Java build path 为 main 以及 test下的各个文件夹
4. 项目右键 Configure 选则 convert to maven（前提是eclipse已经配置好maven 以及）
参见文章（maven3.5以以上选用阿里云仓库）：https://blog.csdn.net/flower_CSDN/article/details/79946008
5. jdk1.7


#### 安装使用教程
1. 基于mysql，创建bb 以及 bb_demo(utf-8编码),导入bb.sql 以及  bb_demo.sql（具体数据库可在 jdbc.config中修改），目前项目正在开发中所以可能会出现sql脚本运行有问题，推荐直接从我系统中直接down一份数据库脚本。
2. 运行 com.RunEovaOSS 
3. 访问  http://127.0.0.1:801/
4. 相关资源的配置:resources/dev/ 或者  resources/default/ 前者优先生效，目前已经配置了appid 以及 测试数据库（请不要乱编辑数据库）
5. 演示网址：http://www.bblocks.cn

演示系统老有人乱改，把密码修改了。

#### 简单对象视图管理系统（copy自EOVA）
Easy 简单开发
Object 元数据驱动业务
View 常用功能界面
Admin 信息管理和维护
简单来说就是表/视图对象话（系统只会制作几种常见组织形式），子字段元素话，在此基础组织成视图，具体理解见具体功能。

#### 开发群
BB交流群：818129789！



#### 效果展示
![快速登录](http://zhaojin123.oss-cn-beijing.aliyuncs.com/login.png)
![查询视图](http://zhaojin123.oss-cn-beijing.aliyuncs.com/list.png)
![新增](http://zhaojin123.oss-cn-beijing.aliyuncs.com/insert.png)
![更新](http://zhaojin123.oss-cn-beijing.aliyuncs.com/update.png)
![查看](http://zhaojin123.oss-cn-beijing.aliyuncs.com/view.png)
![删除](http://zhaojin123.oss-cn-beijing.aliyuncs.com/delete.png)
![查询视图](http://zhaojin123.oss-cn-beijing.aliyuncs.com/view2.png)
其他视图模式陆续新增中~~

以上功能只要做以下几种配置即可完成~_~
![表/视图 元素导入](http://zhaojin123.oss-cn-beijing.aliyuncs.com/import.png)
![编辑元素](http://zhaojin123.oss-cn-beijing.aliyuncs.com/edit.png)
![添加菜单](http://zhaojin123.oss-cn-beijing.aliyuncs.com/menu.png)
演示视频：https://v.youku.com/v_show/id_XMzc0Njk3MjEyMA==.html?spm=a2h0j.11185381.listitem_page1.5~A


#### 缺失功能
1. 系统管理以及平台维护未能完成layui（完成系统管理layui改在，原则上暂时不调整平台维护模块）
2. 视图模式目前只完成了，单list以及父子结构list，组织机构，图表
3. 项目中使用出现的缺陷：
a、单表快速形成以及编辑的可视化 
b、表单中增加额外的要素只能生成页面定制 （目前出现的审核界面 表单数据以及会出现其他表的列表以供查看 自动组装不能满足需求）
c、修改数据可能出现多次以及多用户怎么控制 只能修改某字段 
d、现在的默认值基本都是靠形成页面中写入默认值然后隐藏（有一定风险）
e、下拉或者选择框的数据需要送参数，比如本条数据的某字段导致此页面只能生成二次开发（虽然很快但是很麻烦）
f、复杂的table，由于layui的支持问题还有缺陷
g、元对象的拦截对象是不是需要给生成配置好算了？省的用户还得自己建立
h、用户模块和用户的自身模块集成还需要一定工作量



