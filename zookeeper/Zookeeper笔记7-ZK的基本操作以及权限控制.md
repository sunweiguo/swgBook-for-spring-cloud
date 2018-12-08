# Zookeeper笔记7-ZK的基本操作以及权限控制

## 基本数据模型

* 树形结构，每个节点成为znode，它可以有子节点，也可以有数据
* 临时节点和永久节点，临时节点在客户端断开后消失
* 每个zk节点都有各自的版本号，可以通过命令行来显示节点信息
* 每当节点数据发生变化，那么该节点的版本号会累加（**乐观锁**）
* 删除/修改过时节点，版本号不匹配则会报错
* 每个zk节点存储的数据不宜过大，几k即可
* 节点可以设置acl，可以通过权限来限制用户的访问

## zk的作用

* master选举，保证集群是高可用的
* 统一配置文件管理，即只需要部署一台服务器，则可以把相同的配置文件同步更新到其他所有服务器
* 发布与订阅，dubbo发布者把数据存在znode上，订阅者可以读取这个数据
* 分布式锁
* 集群管理，集群中保证数据的强一致性

## zk的基本操作

* `ls /` 显示根节点名称
* `ls2 /` 显示了根节点的状态信息（stat也可以看状态）
* `get /` 拿出节点的数据和信息
* `create [-s] [-e] path data acl` 创建节点，如果是默认创建，则是非顺序的、 持久的节点。加上-e则是临时节点；加上-s表示顺序节点
* **【注1】**：如果是持久节点，状态信息中的ephemeralOwner=0x0；临时节点的这个属性，是后面一串比较长的字符
* **【注2】**：客户端断开连接了，一段时间之后，那么临时节点就会消失（主要是有个时效，超出这个时间还不收到来自客户端的心跳包则才认定客户端挂了）
* **【注3】**：在加上-s后，创建的节点会重命名为一个累加的名称
* `set path newData` 每次修改值后`dataVersion`数据版本号会增1
* **【注4】**：如何实现乐观锁？`set path data version`,就是说带上版本号，如果这个版本不对应，那么就修改失败
* `delete path version` 删除节点


## watcher机制

* 针对每个节点的操作，都会有一个监督者`watcher`
* 当监控的某个对象(znode)发生了变化，则触发watcher事件
* zk中watcher是一次性的，触发后立即销毁（用其他的开源客户端开源让其不会销毁，重复触发）
* 父节点以及他的子孙们的 增 删 改 都能够触发其watcher
* 针对不同类型的事件，触发的watcher事件也不同：
    * （子）节点创建事件
    * （子）节点删除事件
    * （子）节点数据变化事件
* 通过`get path [watch]`或者`stat path [watch]`或者`ls path [watch]`都可以设置watcher
* 父节点 增 删 改 操作触发watcher
* 子节点 增 删 改 操作触发watcher
* 【创建父节点触发】：NodeCreated


```properties
[zk: localhost:2181(CONNECTED) 24] stat /hello watch  
Node does not exist: /hello
[zk: localhost:2181(CONNECTED) 25] create /hello world

WATCHER::
Created /hello

WatchedEvent state:SyncConnected type:NodeCreated path:/hello
```

* 【修改父节点数据触发】：NodeDataChanged


```
[zk: localhost:2181(CONNECTED) 26] stat /hello watch

[zk: localhost:2181(CONNECTED) 27] get /hello
world

[zk: localhost:2181(CONNECTED) 28] set /hello lalala

WATCHER::cZxid = 0x300000011

WatchedEvent state:SyncConnected type:NodeDataChanged path:/helloctime = Sat Dec 08 20:00:53 CST 2018
```

* 【删除父节点触发】：NodeDeleted


```
[zk: localhost:2181(CONNECTED) 32] get /hello watch

[zk: localhost:2181(CONNECTED) 33] delete /hello

WATCHER::
[zk: localhost:2181(CONNECTED) 34]
WatchedEvent state:SyncConnected type:NodeDeleted path:/hello
```

* 【创建子节点触发】：ls为父节点设置watcher，创建子节点触发NodeChildrenChanged


```
[zk: localhost:2181(CONNECTED) 52] ls /
[zookeeper]
[zk: localhost:2181(CONNECTED) 53] create /hello world
Created /hello
[zk: localhost:2181(CONNECTED) 54] ls /hello watch
[]
[zk: localhost:2181(CONNECTED) 55] create /hello/helloson worldson

WATCHER::Created /hello/helloson


WatchedEvent state:SyncConnected type:NodeChildrenChanged path:/hello
```

* 【删除子节点触发】：ls为父节点设置watcher，删除子节点触发NodeChildrenChanged


```
[zk: localhost:2181(CONNECTED) 56] ls /hello
[helloson]
[zk: localhost:2181(CONNECTED) 57] ls /hello watch
[helloson]
[zk: localhost:2181(CONNECTED) 58] delete /hello/helloson

WATCHER::
[zk: localhost:2181(CONNECTED) 59]
WatchedEvent state:SyncConnected type:NodeChildrenChanged path:/hello
```


* 【更新子节点触发】：ls为父节点设置watcher，更新子节点不触发事件

```
[zk: localhost:2181(CONNECTED) 59] ls /hello
[]
[zk: localhost:2181(CONNECTED) 60] create /hello/helloson worldson
Created /hello/helloson
[zk: localhost:2181(CONNECTED) 61] ls /hello watch 
[helloson]
[zk: localhost:2181(CONNECTED) 62] set /hello/helloson worldsonhahaha
cZxid = 0x300000020
ctime = Sat Dec 08 20:15:05 CST 2018
mZxid = 0x300000021
mtime = Sat Dec 08 20:16:06 CST 2018
pZxid = 0x300000020
cversion = 0
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 14
numChildren = 0
[zk: localhost:2181(CONNECTED) 63] get /hello/helloson
worldsonhahaha
```

就算是设置成`ls /hello/helloson watch`也不行。只有这样才可以触发watcher:


```
[zk: localhost:2181(CONNECTED) 72] get /swg/swgson watch
8080

[zk: localhost:2181(CONNECTED) 74] set /swg/swgson 7070

WATCHER::
cZxid = 0x300000024

WatchedEvent state:SyncConnected type:NodeDataChanged path:/swg/swgsonctime = Sat Dec 08 20:18:53 CST 2018
```

为什么更新子节点的时候ls不行，但是get就行呢？客户端要想收到更新子节点内容的消息的话，不能通过子节点的事件来触发，必须把子节点当作父节点来做。然而`ls path [watch] ：查询指定路径下的子节点`所以是针对的子节点，所以不能触发这个事件；而`get path [watch] : 查询指定节点中的数据，如果节点中有数据`或者`stat path [watch] : 查询指定节点的一些描述信息`这些直接是操作这个节点，把这个节点当作是父节点，所以能起作用。

## watcher使用场景

Watcher是ZK中很重要的特性，ZK允许用户在指定节点上注册一些Watcher，在该节点相关特定事件（比如节点添加、删除、子节点变更等）发生时Watcher会监听到，ZK服务端会将事件通知到感兴趣的客户端上去，该机制是ZK实现分布式协调服务的重要特性。

通知的时候服务端只会告诉客户端一个简单的事件（通知状态、事件类型、节点路径）而不包含具体的变化信息（如原始数据及变更后的数据），客户端如要具体信息再次主动去重新获取数据；此外，无论是服务端还是客户端，只要Watcher被触发ZK就会将其删除，因此在Watcher的使用上需要反复注册，这样轻量的设计有效减轻了服务端压力，如果Watcher一直有效，节点更新频繁时服务端会不断向客户端发送通知，对网络及服务端性能影响会非常大。


比如统一资源配置。


## ACL 权限控制列表

* 针对节点可以设置相关读写等权限，目的是为了保障数据安全性
* 权限permissions可以指定不同的权限范围以及角色
* `getAcl`：获取某个节点的acl权限信息
* `setAcl`：设置某个节点的acl权限信息
* `addauth`：注册某个用户，要把某个用户的用户名和密码输入到系统中进行注册，用户才能登陆。
* 默认权限：


```
[zk: localhost:2181(CONNECTED) 76] getAcl /swg
'world,'anyone
: cdrwa
```


* ACL构成：zk的acl通过[scheme:id:permissions]来构成权限列表，其中`scheme`指采用的某种权限机制；`id`指允许访问的用户；`permissions`指权限组合字符串
* `scheme`：主要是四种
    * `world`：`world`下只有一个`id`，即只有一个用户，也就是`anyone`，那么组合的写法就是`world:anyone:[permissions]`
    * `auth`：代表认证登陆，需要注册用户有权限就可以，形式为`auth:user:password:[permissions]`,密码是明文
    * `degest`：需要对密码加密才能访问，组合形式为`digest:username:BASE64(SHA(password)):[permissions]`，密码是加密的
    * `ip`：当设置为ip指定的ip地址，此时限制ip进行访问，比如`ip:192.168.1.1:[permissions]`
    * `super`：代表超级管理员，拥有所有权限
* `id`：代表允许访问的用户
* `permissions`：
    * `c`：create,创建当前节点的子节点权限
    * `r`：read,获取当前节点或者子节点列表
    * `w`：write,设置当前节点的数据
    * `d`：delete，删除子节点
    * `a`：admin，是比较高的权限，可以去设置和修改权限，即拥有分配权限的权限


* `world:anyone:cdrwa`：对于默认权限，我们可以修改他的权限字符串，如`setAcl path world:anyone:crwa`
* `auth`和`digest`：先`addauth digest username:password（明文密码）`注册用户,然后`setAcl path auth:username:password（明文密码）:cdrwa`就可以设置ACL了。再去`getAcl path`查询到的密码时加密后的。
* `digest`：要先退出刚才的`auth`的账号，直接重启当前客户端即可。`setAcl path digest:username:password（密文密码）:cdrwa`。再去`getAcl path`查询到的密码时加密后的。此时访问、删除、创建节点入`get path`是需要登陆的，即先`addauth digest username:password（明文密码）`登陆。
* `ip`：`setAcl path ip:192.168.1.1:cdrwa`
* `super`：最高权限，修改`zkServer.sh`增加super管理员，重启`zkServer.sh`。到`bin`目录下修改`zkServer.sh`增加配置：

找到这一行：

```
nohup $JAVA "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}"
```

在后面继续添加：

```
"-Dzookeeper.DigestAuthenticationProvider.superDigest=username:xQJmxLMiHGwaqBvst5y6rkB6HQs="
```


## ACL常用使用场景

* 开发/测试环境分离，开发者无权限操作测试库的节点，只能看。比如分为开发节点和测试节点。
* 生产环境上控制指定ip的服务可以访问相关节点防止混乱


## zk四字命令

* zk可以通过它自身提供的简写命令来和服务器进行交互
* 需要使用到 `nc` 命令，`yum install nc`
* `echo [command] | nc [ip] [port]`
    * 【stat】查看zk的状态信息，以及是单机还是集群状态：`echo stat | nc ip或者localhost 2181`
    * 【ruok】查看当前zkServer是否启动，正常返回imok：`echo ruok | nc ip 2181`
    * 【dump】列出未经处理的会话和临时节点：`echo dump | nc ip 2181`
    * 【conf】查看服务配置
    * 【cons】展示连接到服务器的客户端信息
    * 【envi】环境变量，显示jdk和zk等环境变量的信息
    * 【mntr】监控zk健康信息
    * 【wchs】展示watch的信息
    * 【wchc】与【wchp】：分别展示session与watch及path与watcher的信息，默认这两个命令是不能访问的，需要将他们列入白名单才行