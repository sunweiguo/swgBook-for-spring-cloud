# Zookeeper笔记10-Apache Curator客户端的使用（一）

## 一、前言

对于上一章中应用的java 原生API来操作节点。来看看他的不足：
* 超时重连，不支持自动，需要手动操作
* watcher注册一次后会失效
* 不支持递归创建节点

对于Apache Curator开源客户端，具有以下的优点：
* Apache的开源项目，值得信赖
* 解决watcher的注册一次就失效的问题
* API更加简单易用
* 提供更多解决方案并且实现简单，比如分布式锁
* 提供常用的zookeeper工具类
* 编程风格更爽


本篇文章为上半部分，主要学习了一下自动重连、创建节点、查询节点数据和子节点、删除和修改节点数据。还有就是用nodeCache以及PathChildrenCache缓存节点数据来解决注册一次就失效的问题。
## 二、使用

首先新建一个maven工程，我这里直接新建了一个SpringBoot工程，依赖：

```xml
<!--curator-->
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
    <version>4.0.0</version>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>4.0.0</version>
</dependency>
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
    <version>3.4.11</version>
</dependency>
```


## 三、连接&自动重连

配置完依赖后，我们就可以来写一个简单的demo测试与zookeeper服务端的连接。代码如下：


```java
public class CuratorConnect {
    public CuratorFramework client = null;
    private static final String zkServerPath = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";

    public CuratorConnect(){
        /**
         * （推荐）
         * 同步创建zk示例，原生api是异步的
         * 这一步是设置重连策略
         *
         * 构造器参数：
         *  curator链接zookeeper的策略:ExponentialBackoffRetry
         *  baseSleepTimeMs：初始sleep的时间
         *  maxRetries：最大重试次数
         *  maxSleepMs：最大重试时间
         */
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,5);
        client = CuratorFrameworkFactory.builder()
                .connectString(zkServerPath)
                .sessionTimeoutMs(10*1000)
                .retryPolicy(retryPolicy)
                .build();

        client.start();
    }

    private void closeZKClient(){
        if(client != null){
            client.close();
        }
    }

    public static void main(String[] args) {
        CuratorConnect curatorConnect = new CuratorConnect();
        boolean isZkClientStart = curatorConnect.client.isStarted();

        System.out.println("客户端是否打开:"+isZkClientStart);

        curatorConnect.closeZKClient();

        isZkClientStart = curatorConnect.client.isStarted();

        System.out.println("客户端是否打开:"+isZkClientStart);
    }
}
```

curator连接zookeeper服务器时有自动重连机制，而curator的重连策略有五种。

* 第一种就是上面提到的：

```java
/**
 * （推荐）
 * 同步创建zk示例，原生api是异步的
 * 这一步是设置重连策略
 * 
 * 构造器参数：
 *  curator链接zookeeper的策略:ExponentialBackoffRetry
 *  baseSleepTimeMs：初始sleep的时间
 *  maxRetries：最大重试次数
 *  maxSleepMs：最大重试时间
 */
RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
```


* 第二种，可设定重连n次：


```java
/**
 * （推荐）
 * curator链接zookeeper的策略:RetryNTimes
 * 
 * 构造器参数：
 * n：重试的次数
 * sleepMsBetweenRetries：每次重试间隔的时间
 */
RetryPolicy retryPolicy = new RetryNTimes(3, 5000);
```
* 第三种，只会重连一次：


```java
/**
 * （不推荐）
 * curator链接zookeeper的策略:RetryOneTime
 * 
 * 构造器参数：
 * sleepMsBetweenRetry:每次重试间隔的时间
 * 这个策略只会重试一次
 */
RetryPolicy retryPolicy2 = new RetryOneTime(3000);
```


* 第四种，永远重连：


```java
/**
 * 永远重试，不推荐使用
 */
RetryPolicy retryPolicy3 = new RetryForever(retryIntervalMs)
```

* 第五种，可设定最大重试时间：


```java
/**
 * curator链接zookeeper的策略:RetryUntilElapsed
 * 
 * 构造器参数：
 * maxElapsedTimeMs:最大重试时间
 * sleepMsBetweenRetries:每次重试间隔
 * 重试时间超过maxElapsedTimeMs后，就不再重试
 */
RetryPolicy retryPolicy4 = new RetryUntilElapsed(2000, 3000);
```

## 四、zk命名空间以及创建节点

zookeeper的命名空间就类似于我们平时使用Eclipse等开发工具的工作空间一样，我们该连接中所有的操作都是基于这个命名空间的。curator提供了设置命名空间的方法，这样我们任何的连接都可以去设置一个命名空间。设置了命名空间并成功连接后，zookeeper的根节点会多出一个以命名空间名称所命名的节点。然后我们在该连接的增删查改等操作都会在这个节点中进行。

```java
public class CuratorCreateNode {
    // Curator客户端
    public CuratorFramework client = null;
    // 集群模式则是多个ip
    private static final String zkServerIps = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";

    public CuratorCreateNode() {

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);

        // 实例化Curator客户端
        client = CuratorFrameworkFactory.builder() // 使用工厂类来建造客户端的实例对象
                .connectString(zkServerIps)  // 放入zookeeper服务器ip
                .sessionTimeoutMs(10000).retryPolicy(retryPolicy)  // 设定会话时间以及重连策略
                .namespace("workspace").build();  // 设置命名空间以及开始建立连接

        // 启动Curator客户端
        client.start();
    }

    // 关闭zk客户端连接
    private void closeZKClient() {
        if (client != null) {
            this.client.close();
        }
    }

    public static void main(String[] args) throws Exception {
        // 实例化
        CuratorCreateNode curatorConnect = new CuratorCreateNode();
        // 获取当前客户端的状态
        boolean isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

        // 创建节点
        String nodePath = "/super/testNode";  // 节点路径
        byte[] data = "this is a test data".getBytes();  // 节点数据
        String result = curatorConnect.client.create().creatingParentsIfNeeded()  // 创建父节点，也就是会递归创建
                .withMode(CreateMode.PERSISTENT)  // 节点类型
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)  // 节点的acl权限
                .forPath(nodePath, data);

        System.out.println(result + "节点，创建成功...");

        Thread.sleep(1000);

        // 关闭客户端
        curatorConnect.closeZKClient();

        // 获取当前客户端的状态
        isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));
    }
}
```
运行该类，控制台输出信息如下：


```
当前客户端的状态：连接中...
/super/testNode节点，创建成功...
当前客户端的状态：已关闭...
```

## 五、修改节点以及删除节点


```java
public class CuratorConnect {

    // Curator客户端
    public CuratorFramework client = null;
    // 集群模式则是多个ip
    private static final String zkServerIps = "192.168.190.128:2181,192.168.190.129:2181,192.168.190.130:2181";

    public CuratorConnect() {

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);

        // 实例化Curator客户端
        client = CuratorFrameworkFactory.builder() // 使用工厂类来建造客户端的实例对象
                .connectString(zkServerIps)  // 放入zookeeper服务器ip
                .sessionTimeoutMs(10000).retryPolicy(retryPolicy)  // 设定会话时间以及重连策略
                .namespace("workspace").build();  // 设置命名空间以及开始建立连接

        // 启动Curator客户端
        client.start();
    }

    // 关闭zk客户端连接
    private void closeZKClient() {
        if (client != null) {
            this.client.close();
        }
    }

    public static void main(String[] args) throws Exception {
        // 实例化
        CuratorConnect curatorConnect = new CuratorConnect();
        // 获取当前客户端的状态
        boolean isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

        // 节点路径
        String nodePath = "/super/testNode";

        // 更新节点数据
        byte[] newData = "this is a new data".getBytes();
        Stat resultStat = curatorConnect.client.setData().withVersion(0)  // 指定数据版本
                .forPath(nodePath, newData);  // 需要修改的节点路径以及新数据

        System.out.println("更新节点数据成功，新的数据版本为：" + resultStat.getVersion());

        // 删除节点
        curatorConnect.client.delete()
                .guaranteed()  // 如果删除失败，那么在后端还是会继续删除，直到成功
                .deletingChildrenIfNeeded()  // 子节点也一并删除，也就是会递归删除
                .withVersion(resultStat.getVersion())
                .forPath(nodePath);

        Thread.sleep(1000);

        // 关闭客户端
        curatorConnect.closeZKClient();

        // 获取当前客户端的状态
        isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));
    }
}
```
## 六、查询节点相关信息
1.获取某个节点的数据

```java
...
public class CuratorConnect {
    ...
    public static void main(String[] args) throws Exception {
        // 实例化
        CuratorConnect curatorConnect = new CuratorConnect();
        // 获取当前客户端的状态
        boolean isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

        // 节点路径
        String nodePath = "/super/testNode";

        // 读取节点数据
        Stat stat = new Stat();
        byte[] nodeData = curatorConnect.client.getData().storingStatIn(stat).forPath(nodePath);
        System.out.println("节点 " + nodePath + " 的数据为：" + new String(nodeData));
        System.out.println("该节点的数据版本号为：" + stat.getVersion());

        Thread.sleep(1000);

        // 关闭客户端
        curatorConnect.closeZKClient();

        // 获取当前客户端的状态
        isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));
    }
}
```
2.获取某个节点下的子节点列表，现有一个节点的子节点列表如下：


```
[zk: localhost:2181(CONNECTED) 33] ls /workspace/super/testNode
[threeNode, twoNode, oneNode]
[zk: localhost:2181(CONNECTED) 34]
```


```java
...
public class CuratorConnect {
    ...
    public static void main(String[] args) throws Exception {
        // 实例化
        CuratorConnect curatorConnect = new CuratorConnect();
        // 获取当前客户端的状态
        boolean isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

        // 节点路径
        String nodePath = "/super/testNode";

        // 获取子节点列表
        List<String> childNodes = curatorConnect.client.getChildren().forPath(nodePath);
        System.out.println(nodePath + " 节点下的子节点列表：");
        for (String childNode : childNodes) {
            System.out.println(childNode);
        }

        Thread.sleep(1000);

        // 关闭客户端
        curatorConnect.closeZKClient();

        // 获取当前客户端的状态
        isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));
    }
}
```
3.查询某个节点是否存在


```java
...
public class CuratorConnect {
    ...
    public static void main(String[] args) throws Exception {
        // 实例化
        CuratorConnect curatorConnect = new CuratorConnect();
        // 获取当前客户端的状态
        boolean isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

        // 节点路径
        String nodePath = "/super/testNode";

        // 查询某个节点是否存在，存在就会返回该节点的状态信息，如果不存在的话则返回空
        Stat statExist = curatorConnect.client.checkExists().forPath(nodePath);
        if (statExist == null) {
            System.out.println(nodePath + " 节点不存在");
        } else {
            System.out.println(nodePath + " 节点存在");
        }

        Thread.sleep(1000);

        // 关闭客户端
        curatorConnect.closeZKClient();

        // 获取当前客户端的状态
        isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));
    }
}
```

## 七、curator之usingWatcher


curator在注册watch事件上，提供了一个usingWatcher方法，使用这个方法注册的watch事件和默认watch事件一样，监听只会触发一次，监听完毕后就会销毁，也就是一次性的。而这个方法有两种参数可选，一个是zk原生API的Watcher接口的实现类，另一个是Curator提供的CuratorWatcher接口的实现类，不过在usingWatcher方法上使用哪一个效果都是一样的，都是一次性的。

新建一个 MyWatcher 实现类，实现 Watcher 接口。代码如下：


```
/**
 * @program: zookeeper-connection
 * @description:  zk原生API的Watcher接口实现
 * @author: 01
 * @create: 2018-04-28 13:41
 **/
public class MyWatcher implements Watcher {

    // Watcher事件通知方法
    public void process(WatchedEvent watchedEvent) {
        System.out.println("触发watcher，节点路径为：" + watchedEvent.getPath());
    }
}
```

新建一个 MyCuratorWatcher 实现类，实现 CuratorWatcher 接口。代码如下：



```
/**
 * @program: zookeeper-connection
 * @description: Curator提供的CuratorWatcher接口实现
 * @author: 01
 * @create: 2018-04-28 13:40
 **/
public class MyCuratorWatcher implements CuratorWatcher {

    // Watcher事件通知方法
    public void process(WatchedEvent watchedEvent) throws Exception {
        System.out.println("触发watcher，节点路径为：" + watchedEvent.getPath());
    }
}
```

```
...java
public class CuratorConnect {
    ...
    public static void main(String[] args) throws Exception {
        // 实例化
        CuratorConnect curatorConnect = new CuratorConnect();
        // 获取当前客户端的状态
        boolean isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

        // 节点路径
        String nodePath = "/super/testNode";

        // 添加 watcher 事件，当使用usingWatcher的时候，监听只会触发一次，监听完毕后就销毁
        curatorConnect.client.getData().usingWatcher(new MyCuratorWatcher()).forPath(nodePath);
        // curatorConnect.client.getData().usingWatcher(new MyWatcher()).forPath(nodePath);

        Thread.sleep(100000);

        // 关闭客户端
        curatorConnect.closeZKClient();

        // 获取当前客户端的状态
        isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));
    }
}
```
运行该类，然后到zookeeper服务器上修改/super/testNode节点的数据：


```
[zk: localhost:2181(CONNECTED) 35] set /workspace/super/testNode new-data
cZxid = 0xb00000015
ctime = Sat Apr 28 20:59:57 CST 2018
mZxid = 0xb0000002b
mtime = Sat Apr 28 21:40:58 CST 2018
pZxid = 0xb0000001c
cversion = 3
dataVersion = 2
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 8
numChildren = 3
[zk: localhost:2181(CONNECTED) 36]
```

修改完成后，此时控制台输出内容如下，因为workspace是命名空间节点，所以不会被打印出来：


```
触发watcher，节点路径为：/super/testNode
```

## 八、curator之nodeCache一次注册N次监听

想要实现watch一次注册n次监听的话，我们需要使用到curator里的一个NodeCache对象。这个对象可以用来缓存节点数据，并且可以给节点添加nodeChange事件，当节点的数据发生变化就会触发这个事件。


```java
...
public class CuratorConnect {
    ...
    public static void main(String[] args) throws Exception {
        // 实例化
        CuratorConnect curatorConnect = new CuratorConnect();
        // 获取当前客户端的状态
        boolean isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

        // 节点路径
        String nodePath = "/super/testNode";

        // NodeCache: 缓存节点，并且可以监听数据节点的变更，会触发事件
        final NodeCache nodeCache = new NodeCache(curatorConnect.client, nodePath);

        // 参数 buildInitial : 初始化的时候获取node的值并且缓存
        nodeCache.start(true);

        // 获取缓存里的节点初始化数据
        if (nodeCache.getCurrentData() != null) {
            System.out.println("节点初始化数据为：" + new String(nodeCache.getCurrentData().getData()));
        } else {
            System.out.println("节点初始化数据为空...");
        }

        // 为缓存的节点添加watcher，或者说添加监听器
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            // 节点数据change事件的通知方法
            public void nodeChanged() throws Exception {
                // 防止节点被删除时发生错误
                if (nodeCache.getCurrentData() == null) {
                    System.out.println("获取节点数据异常，无法获取当前缓存的节点数据，可能该节点已被删除");
                    return;
                }
                // 获取节点最新的数据
                String data = new String(nodeCache.getCurrentData().getData());
                System.out.println(nodeCache.getCurrentData().getPath() + " 节点的数据发生变化，最新的数据为：" + data);
            }
        });

        Thread.sleep(200000);

        // 关闭客户端
        curatorConnect.closeZKClient();

        // 获取当前客户端的状态
        isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));
    }
}
```
运行该类后，我们到zookeeper服务器上，对/super/testNode节点进行如下操作：


```
[zk: localhost:2181(CONNECTED) 2] set /workspace/super/testNode change-data     
cZxid = 0xb00000015
ctime = Sat Apr 28 20:59:57 CST 2018
mZxid = 0xb00000037
mtime = Sat Apr 28 23:49:42 CST 2018
pZxid = 0xb0000001c
cversion = 3
dataVersion = 6
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 11
numChildren = 3      
[zk: localhost:2181(CONNECTED) 3] set /workspace/super/testNode change-agin-data
cZxid = 0xb00000015
ctime = Sat Apr 28 20:59:57 CST 2018
mZxid = 0xb00000038
mtime = Sat Apr 28 23:50:01 CST 2018
pZxid = 0xb0000001c
cversion = 3
dataVersion = 7
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 16
numChildren = 3
[zk: localhost:2181(CONNECTED) 8] delete /workspace/super/testNode
[zk: localhost:2181(CONNECTED) 9] create /workspace/super/testNode test-data
Created /workspace/super/testNode
[zk: localhost:2181(CONNECTED) 10]
```

此时控制台输出内容如下：


```
当前客户端的状态：连接中...
节点初始化数据为：new-data
/super/testNode 节点的数据发生变化，最新的数据为：change-data
/super/testNode 节点的数据发生变化，最新的数据为：change-agin-data
获取节点数据异常，无法获取当前缓存的节点数据，可能该节点已被删除
/super/testNode 节点的数据发生变化，最新的数据为：test-data
当前客户端的状态：已关闭...
```

从控制台输出的内容可以看到，只要数据发生改变了都会触发这个事件，并且是可以重复触发的，而不是一次性的。

## 九、curator之PathChildrenCache子节点监听

使用NodeCache虽然能实现一次注册n次监听，但是却只能监听一个nodeChanged事件，也就是说创建、删除以及子节点的事件都无法监听。如果我们要监听某一个节点的子节点的事件，或者监听某一个特定节点的增删改事件都需要借助PathChildrenCache来实现。从名称上可以看到，PathChildrenCache也是用缓存实现的，并且也是一次注册n次监听。当我们传递一个节点路径时是监听该节点下的子节点事件，如果我们要限制监听某一个节点，只需要加上判断条件即可。


```java
...
public class CuratorConnect {
    ...
    public static void main(String[] args) throws Exception {
        // 实例化
        CuratorConnect curatorConnect = new CuratorConnect();
        // 获取当前客户端的状态
        boolean isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

        // 父节点路径
        String nodePath = "/super/testNode";

        // 为子节点添加watcher
        // PathChildrenCache: 监听数据节点的增删改，可以设置触发的事件
        final PathChildrenCache childrenCache = new PathChildrenCache(curatorConnect.client, nodePath, true);

        /**
         * StartMode: 初始化方式
         * POST_INITIALIZED_EVENT：异步初始化，初始化之后会触发事件
         * NORMAL：异步初始化
         * BUILD_INITIAL_CACHE：同步初始化
         */
        childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

        // 列出子节点数据列表，需要使用BUILD_INITIAL_CACHE同步初始化模式才能获得，异步是获取不到的
        List<ChildData> childDataList = childrenCache.getCurrentData();
        System.out.println("当前节点的子节点详细数据列表：");
        for (ChildData childData : childDataList) {
            System.out.println("\t* 子节点路径：" + new String(childData.getPath()) + "，该节点的数据为：" + new String(childData.getData()));
        }

        // 添加事件监听器
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                // 通过判断event type的方式来实现不同事件的触发
                if (event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)) {  // 子节点初始化时触发
                    System.out.println("\n--------------\n");
                    System.out.println("子节点初始化成功");
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {  // 添加子节点时触发
                    System.out.println("\n--------------\n");
                    System.out.print("子节点：" + event.getData().getPath() + " 添加成功，");
                    System.out.println("该子节点的数据为：" + new String(event.getData().getData()));
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {  // 删除子节点时触发
                    System.out.println("\n--------------\n");
                    System.out.println("子节点：" + event.getData().getPath() + " 删除成功");
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {  // 修改子节点数据时触发
                    System.out.println("\n--------------\n");
                    System.out.print("子节点：" + event.getData().getPath() + " 数据更新成功，");
                    System.out.println("子节点：" + event.getData().getPath() + " 新的数据为：" + new String(event.getData().getData()));
                }
            }
        });

        Thread.sleep(200000);

        // 关闭客户端
        curatorConnect.closeZKClient();

        // 获取当前客户端的状态
        isZkCuratorStarted = curatorConnect.client.isStarted();
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));
    }
}
```