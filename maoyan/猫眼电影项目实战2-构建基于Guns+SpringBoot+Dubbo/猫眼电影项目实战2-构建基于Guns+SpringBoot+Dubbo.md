# 猫眼电影项目实战2-构建基于Guns+SpringBoot+Dubbo的框架

## API网关


关于网关，在学习spring cloud的时候，已经详细介绍过了。简单说一说网关的好处，一个是可以屏蔽掉访问内部接口的细节，比如ip地址和端口号，网关直接给你一个入口，你调用这个无内部细节的入口就可以调用到里面实际的服务器的接口，一定程度上保证了安全性；另一个是用户访问的服务，往往是几个服务的串联，比如用户查看订单，那么就要先判断是否登陆，可能走的是UserService，然后要将订单调出来，同时订单里面有商品信息，那么还要走ProductService，很麻烦，网关可以聚合这些服务，给一个统一入口，用户只要调一次即可。还有其他的一些好处，网关可以做安全校验，可以做负载均衡，可以做静态处理，可以做检测，可以屏蔽掉不同系统的通信协议等等等。


总结来说，API网关是微服务系统的门面。


<div align="center">
    <img src="../../pic/maoyan/猫眼电影项目实战2-1.png" >
</div>

## Guns

[Guns](https://gitee.com/naan1993/guns/tree/v3.1/)基于SpringBoot,致力于做更简洁的后台管理系统,完美整合springmvc + shiro + mybatis-plus + beetl + flowable的一个开源整合系统。基于他，我们可以快速地搭建应用系统。这里主要用的是V4.0版本。

首先根据guns-rest下的db创建响应的数据库和表。并注意application.yml中的连接数据库的用户名和密码。

启动guns-rest的时候可能会报错。

第一次报错：提示log4j包找不到.我们只需要去给他添加依赖即可：


```xml
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.17</version>
</dependency>
```

第二次报错说myabtis那边连接有点问题。修改application.yml下数据源的url为：


```
url: jdbc:mysql://127.0.0.1:3306/guns_rest?autoReconnect=true&useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
```


这样就应该可以正常启动了，为了测试，在浏览器中输入：

http://localhost/auth?userName=admin&password=admin

看是否返回一个token。这是[JWT](https://www.jianshu.com/p/576dbf44b2ae)的一个应用。

那么guns基本就引入成功了。



## API网关

拷贝一份guns-rest，重新命名为guns-api，作为我们的API网关服务。他要依赖于dubbo和zookeeper。所以要整合这两者。具体不再赘述。

## API公共接口

因为很多接口是公共依赖的，所以需要将这些接口单独抽取出来放在一起，然后统一以jar包的形式提供。这就需要将guns-core拷贝一份叫做guns-api。

可以在里面测试一下，写一个接口`UserAPI`.

然后在guns-gateway中依赖于这个guns-api，然后拿到这个接口。比较简单，具体看代码。


<div align="center">
    <img src="../../pic/maoyan/猫眼电影项目实战2-2.png" >
</div>


注意，别忘记在parent中要将我们新加入的module添加管理。