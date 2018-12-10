# Zookeeper笔记11-Apache Curator客户端的使用（二）

## zk-watcher应用实例之模拟统一更新N台节点的配置文件

zookeeper有一个比较常见的应用场景就是统一管理、更新分布式集群环境中每个节点的配置文件，我们可以在代码中监听集群中的节点，当节点数据发生改变时就同步到其他节点上。如下图：

![image](http://bloghello.oursnail.cn/18-12-10/90520428.jpg)

因为我们使用的json作为节点存储的数据格式，所以需要准备一个工具类来做json与pojo对象的一个转换，也就是所谓的反序列化。创建一个 JsonUtils 类，代码如下：



```java
public class JsonUtils {

    // 定义jackson对象
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 将对象转换成json字符串。
     * <p>Title: pojoToJson</p>
     * <p>Description: </p>
     * @param data
     * @return
     */
    public static String objectToJson(Object data) {
        try {
            String string = MAPPER.writeValueAsString(data);
            return string;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json结果集转化为对象
     *
     * @param jsonData json数据
     * @param beanType 对象中的object类型
     * @return
     */
    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) {
        try {
            T t = MAPPER.readValue(jsonData, beanType);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json数据转换成pojo对象list
     * <p>Title: jsonToList</p>
     * <p>Description: </p>
     * @param jsonData
     * @param beanType
     * @return
     */
    public static <T>List<T> jsonToList(String jsonData, Class<T> beanType) {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            List<T> list = MAPPER.readValue(jsonData, javaType);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
```

需要额外的依赖：

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.5</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.7.4</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.7.4</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
    <version>2.7.4</version>
</dependency>
```

然后创建客户端类，客户端类就是用来监听集群中的节点的。由于是模拟，所以这里的部分代码是伪代码。客户端类我们这里创建了三个，因为集群中有三个节点，由于代码基本上是一样的，每个客户端分别监听watch事件，所以这里只贴出客户端_1的代码。如下：


```java
public class Client_1 {

    public CuratorFramework client = null;
    public static final String zkServerIp = "192.168.190.128:2181";

    // 初始化重连策略以及客户端对象并启动
    public Client_1() {
        RetryPolicy retryPolicy = new RetryNTimes(3, 5000);
        client = CuratorFrameworkFactory.builder()
                .connectString(zkServerIp)
                .sessionTimeoutMs(10000).retryPolicy(retryPolicy)
                .namespace("workspace").build();
        client.start();
    }

    // 关闭客户端
    public void closeZKClient() {
        if (client != null) {
            this.client.close();
        }
    }

    //  public final static String CONFIG_NODE = "/super/testNode/redis-config";
    public final static String CONFIG_NODE_PATH = "/super/testNode";
    public final static String SUB_PATH = "/redis-config";
    public static CountDownLatch countDown = new CountDownLatch(1);  // 计数器

    public static void main(String[] args) throws Exception {
        Client_1 cto = new Client_1();
        System.out.println("client1 启动成功...");

        // 开启子节点缓存
        final PathChildrenCache childrenCache = new PathChildrenCache(cto.client, CONFIG_NODE_PATH, true);
        childrenCache.start(StartMode.BUILD_INITIAL_CACHE);

        // 添加子节点监听事件
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                // 监听节点的数据更新事件
                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
                    String configNodePath = event.getData().getPath();
                    if (configNodePath.equals(CONFIG_NODE_PATH + SUB_PATH)) {
                        System.out.println("监听到配置发生变化，节点路径为:" + configNodePath);

                        // 读取节点数据
                        String jsonConfig = new String(event.getData().getData());
                        System.out.println("节点" + CONFIG_NODE_PATH + "的数据为: " + jsonConfig);

                        // 从json转换配置
                        RedisConfig redisConfig = null;
                        if (StringUtils.isNotBlank(jsonConfig)) {
                            redisConfig = JsonUtils.jsonToPojo(jsonConfig, RedisConfig.class);
                        }

                        // 配置不为空则进行相应操作
                        if (redisConfig != null) {
                            String type = redisConfig.getType();
                            String url = redisConfig.getUrl();
                            String remark = redisConfig.getRemark();
                            // 判断事件
                            if (type.equals("add")) {
                                System.out.println("\n-------------------\n");
                                System.out.println("监听到新增的配置，准备下载...");
                                // ... 连接ftp服务器，根据url找到相应的配置
                                Thread.sleep(500);
                                System.out.println("开始下载新的配置文件，下载路径为<" + url + ">");
                                // ... 下载配置到你指定的目录
                                Thread.sleep(1000);
                                System.out.println("下载成功，已经添加到项目中");
                                // ... 拷贝文件到项目目录
                            } else if (type.equals("update")) {
                                System.out.println("\n-------------------\n");
                                System.out.println("监听到更新的配置，准备下载...");
                                // ... 连接ftp服务器，根据url找到相应的配置
                                Thread.sleep(500);
                                System.out.println("开始下载配置文件，下载路径为<" + url + ">");
                                // ... 下载配置到你指定的目录
                                Thread.sleep(1000);
                                System.out.println("下载成功...");
                                System.out.println("删除项目中原配置文件...");
                                Thread.sleep(100);
                                // ... 删除原文件
                                System.out.println("拷贝配置文件到项目目录...");
                                // ... 拷贝文件到项目目录
                            } else if (type.equals("delete")) {
                                System.out.println("\n-------------------\n");
                                System.out.println("监听到需要删除配置");
                                System.out.println("删除项目中原配置文件...");
                            }
                            // TODO 视情况统一重启服务
                        }
                    }
                }
            }
        });

        countDown.await();

        cto.closeZKClient();
    }
}
```
完成以上代码的编写后，将所有的客户类都运行起来。然后到zookeeper服务器上，进行如下操作：


```
[zk: localhost:2181(CONNECTED) 14] set /workspace/super/testNode/redis-config {"type":"add","url":"ftp://192.168.10.123/config/redis.xml","remark":"add"}
cZxid = 0xc00000039
ctime = Mon Apr 30 01:43:47 CST 2018
mZxid = 0xc00000043
mtime = Mon Apr 30 01:52:35 CST 2018
pZxid = 0xc00000039
cversion = 0
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 75
numChildren = 0
[zk: localhost:2181(CONNECTED) 15] set /workspace/super/testNode/redis-config {"type":"update","url":"ftp://192.168.10.123/config/redis.xml","remark":"update"}
cZxid = 0xc00000039
ctime = Mon Apr 30 01:43:47 CST 2018
mZxid = 0xc00000044
mtime = Mon Apr 30 01:53:46 CST 2018
pZxid = 0xc00000039
cversion = 0
dataVersion = 2
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 81
numChildren = 0
[zk: localhost:2181(CONNECTED) 16] set /workspace/super/testNode/redis-config {"type":"delete","url":"","remark":"delete"}   
cZxid = 0xc00000039               
ctime = Mon Apr 30 01:43:47 CST 2018
mZxid = 0xc00000045
mtime = Mon Apr 30 01:54:06 CST 2018
pZxid = 0xc00000039
cversion = 0
dataVersion = 3
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 44
numChildren = 0
[zk: localhost:2181(CONNECTED) 17]
```

![image](http://bloghello.oursnail.cn/18-12-10/55655494.jpg)

![image](http://bloghello.oursnail.cn/18-12-10/45175373.jpg)

![image](http://bloghello.oursnail.cn/18-12-10/92597884.jpg)

如上，从三个客户端的控制台输出信息可以看到，三个节点都进行了同样操作，触发了同样的watch事件，这样就可以完成统一的配置文件管理。


## curator之acl权限操作与认证授权

我们先演示在创建节点时设置acl权限，现在/workspace/super只有如下节点：


```
[zk: localhost:2181(CONNECTED) 27] ls /workspace/super
[xxxnode, testNode]
```

然后新建一个 CuratorAcl 类，关于acl权限的概念以及部分API代码都在之前的zk原生API使用一文中介绍过了，所以这里就不赘述了。编写代码如下：


```java
public class CuratorAcl {

    // Curator客户端
    public CuratorFramework client = null;
    // 集群模式则是多个ip
    private static final String zkServerIps = "192.168.190.128:2181,192.168.190.129:2181,192.168.190.130:2181";

    public CuratorAcl() {
        RetryPolicy retryPolicy = new RetryNTimes(3, 5000);
        client = CuratorFrameworkFactory.builder().authorization("digest", "user1:123456a".getBytes())  // 认证授权，登录用户
                .connectString(zkServerIps)
                .sessionTimeoutMs(10000).retryPolicy(retryPolicy)
                .namespace("workspace").build();
        client.start();
    }

    public void closeZKClient() {
        if (client != null) {
            this.client.close();
        }
    }

    public static void main(String[] args) throws Exception {

        // 实例化
        CuratorAcl cto = new CuratorAcl();
        boolean isZkCuratorStarted = cto.client.isStarted();
        System.out.println("当前客户的状态：" + (isZkCuratorStarted ? "连接中" : "已关闭"));

        String nodePath = "/super/testAclNode/testOne";

        // 自定义权限列表
        List<ACL> acls = new ArrayList<ACL>();
        Id user1 = new Id("digest", AclUtils.getDigestUserPwd("user1:123456a"));
        Id user2 = new Id("digest", AclUtils.getDigestUserPwd("user2:123456b"));
        acls.add(new ACL(ZooDefs.Perms.ALL, user1));
        acls.add(new ACL(ZooDefs.Perms.READ, user2));
        acls.add(new ACL(ZooDefs.Perms.DELETE | ZooDefs.Perms.CREATE, user2));

        // 创建节点，使用自定义权限列表来设置节点的acl权限
        byte[] nodeData = "child-data".getBytes();
        cto.client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).withACL(acls).forPath(nodePath, nodeData);

        cto.closeZKClient();
        boolean isZkCuratorStarted2 = cto.client.isStarted();
        System.out.println("当前客户的状态：" + (isZkCuratorStarted2 ? "连接中" : "已关闭"));
    }
}
```
运行该类，然后到zookeeper服务器上，通过命令行进行如下操作：


```
[zk: localhost:2181(CONNECTED) 19] ls /workspace/super/testAclNode    
[testOne]
[zk: localhost:2181(CONNECTED) 20] getAcl /workspace/super/testAclNode
'world,'anyone
: cdrwa
[zk: localhost:2181(CONNECTED) 21] getAcl /workspace/super/testAclNode/testOne
'digest,'user1:TQYTqd46qVVbWpOd02tLO5qb+JM=
: cdrwa
'digest,'user2:CV4ED0rE6SxA3h/DN/WyScDMbCs=
: r
'digest,'user2:CV4ED0rE6SxA3h/DN/WyScDMbCs=
: cd
```
如上，可以看到，创建的全部节点的acl权限都是我们设置的自定义权限。

最后我们再来演示如何修改一个已存在的节点的acl权限，修改 CuratorAcl 类中的main方法代码如下：


```java
public static void main(String[] args) throws Exception {
    // 实例化
    CuratorAcl cto = new CuratorAcl();
    boolean isZkCuratorStarted = cto.client.isStarted();
    System.out.println("当前客户的状态：" + (isZkCuratorStarted ? "连接中" : "已关闭"));

    String nodePath = "/super/testAclNodeTwo/testOne";

    // 自定义权限列表
    List<ACL> acls = new ArrayList<ACL>();
    Id user1 = new Id("digest", AclUtils.getDigestUserPwd("user1:123456a"));
    Id user2 = new Id("digest", AclUtils.getDigestUserPwd("user2:123456b"));
    acls.add(new ACL(ZooDefs.Perms.READ | ZooDefs.Perms.CREATE | ZooDefs.Perms.ADMIN, user1));
    acls.add(new ACL(ZooDefs.Perms.READ | ZooDefs.Perms.DELETE | ZooDefs.Perms.CREATE, user2));

    // 设置指定节点的acl权限
    cto.client.setACL().withACL(acls).forPath(nodePath);

    cto.closeZKClient();
    boolean isZkCuratorStarted2 = cto.client.isStarted();
    System.out.println("当前客户的状态：" + (isZkCuratorStarted2 ? "连接中" : "已关闭"));
}
```
运行该类，然后到zookeeper服务器上，通过命令行进行如下操作：


```
[zk: localhost:2181(CONNECTED) 31] getAcl /workspace/super/testAclNodeTwo/testOne
'digest,'user1:TQYTqd46qVVbWpOd02tLO5qb+JM=
: cra
'digest,'user2:CV4ED0rE6SxA3h/DN/WyScDMbCs=
: cdr
[zk: localhost:2181(CONNECTED) 32]
```

可以看到，成功修改了该节点的acl权限。