# Zookeeper笔记6-zk安装和集群搭建

## 前言

回顾一下ZAB协议。Zab协议包括两个核心：

**第一，原子广播**

客户端提交事务请求时Leader节点为每一个请求生成一个事务Proposal，将其发送给集群中所有的Follower节点，收到过半Follower的反馈后开始对事务进行提交。

这也导致了Leader几点崩溃后可能会出现数据不一致的情况，ZAB使用了崩溃恢复来处理数字不一致问题；

消息广播使用了TCP协议进行通讯所有保证了接受和发送事务的顺序性。广播消息时Leader节点为每个事务Proposal分配一个全局递增的ZXID（事务ID），每个事务Proposal都按照ZXID顺序来处理；

Leader节点为每一个Follower节点分配一个队列按事务ZXID顺序放入到队列中，且根据队列的规则FIFO来进行事务的发送。

Follower节点收到事务Proposal后会将该事务以事务日志方式写入到本地磁盘中，成功后反馈Ack消息给Leader节点。

收到过半ACK反馈之后，同时向所有的Follower节点广播Commit消息，Follower节点收到Commit后开始对事务进行提交；

**第二，Master选举**

里面用的算法叫做：Fast Leader Election。

* epoch：选举轮数,即周期，就是之前说的逻辑时钟logicClock
* Zxid: Zxid 是一个 64 位的数字，其中低 32 位是一个简单的单调递增的计数器，针对客户端每一个事务请求，计数器加 1；而高 32 位则代表 Leader 周期 epoch 的编号，每个当选产生一个新的 Leader 服务器，就会从这个 Leader 服务器上取出其本地日志中最大事务的ZXID，并从中读取 epoch 值，然后加 1，以此作为新的 epoch，并将低 32 位从 0 开始计数。


**成为leader的条件**
- 选epoch最大的
- epoch相等，选 zxid 最大的
- epoch和zxid都相等，选择server id最大的（就是我们配置zoo.cfg中的myid）

**选举的基本步骤**
1. 每个从节点都向其他节点发送选自身为Leader的Vote投票请求，等待回复；
2. 从节点接受到的Vote如果比自身的大（ZXID更新）时则投票，并更新自身的Vote，否则拒绝投票；
3. 每个从节点中维护着一个投票记录表，当某个节点收到过半的投票时，结束投票并把该从节点选为Leader，投票结束；


具体一点的步骤：

* 1、发起一轮投票选举，推举自己作为leader，通知所有的服务器，等待接收外部选票。
* 2、只要当前服务器状态为LOOKING，进入循环，不断地读取其它Server发来的通知、进行比较、更新自己的投票、发送自己的投票、统计投票结果，直到leader选出或出错退出。具体做法：
    * 2.1 如果发送过来的逻辑时钟大于目前的逻辑时钟，那么说明这是更新的一次选举投票，此时更新本机的逻辑时钟（logicalclock），清空投票箱（因为已经过期没有用了）调用`totalOrderPredicate`函数判断对方的投票是否优于当前的投票（见下面代码），是的话用对方推荐的leader更新下一次的投票，否则使用初始的投票（投自己），调用`sendNotifications()` 通知所有服务器我的选择，跳到2.4。
    * 2.2 如果对方处于上轮投票，不予理睬，回到2。
    * 2.3 如果对方也处于本轮投票，调用`totalOrderPredicate`函数判断对方的投票是否优于当前的投票，是的话更新当前的投票，否则使用初始的投票（投自己）并新生成`notification`消息放入发送队列。调用`sendNotifications()` 通知所有服务器我的选择。
    * 2.4 将收到的投票放入自己的投票箱中。
    * 2.5 调用计票器的`containsQuorum`函数，判断所推荐的leader是否得到集群多数人的同意，如果得到多数人同意，那么还需等待一段时间，看是否有比当前更优的提议，如果没有，则认为投票结束。根据投票结果修改自己的状态。以上任何一条不满足，则继续循环。

关于`totalOrderPredicate`:

```java
protected boolean totalOrderPredicate(long newId, long newZxid, long newEpoch, long curId, long curZxid, long curEpoch) {
    
    LOG.debug("id: " + newId + ", proposed id: " + curId + ", zxid: 0x" +
    
            Long.toHexString(newZxid) + ", proposed zxid: 0x" + Long.toHexString(curZxid));
    
    // 使用计票器判断当前server的权重是否为0
    if(self.getQuorumVerifier().getWeight(newId) == 0){
    
        return false;
    
    }

    // 通过Epoch、zxid、id来比较两个候选leader
    return ((newEpoch > curEpoch) ||
    
            ((newEpoch == curEpoch) &&
    
            ((newZxid > curZxid) || ((newZxid == curZxid) && (newId > curId)))));
    
    }
```



**总结起来就是一句话：若干个节点，第一次都是投给自己；后面就是，尽量向数据最新的节点靠拢，可以理解为：每个节点贫富有差距，富有的节点让贫穷的节点投自己一票，那么贫穷的节点会接受，反之不行，那么最先拿到超过一半的平穷的节点的投票，就成为leader。（贫穷与富有都是相对的，越富有越可能成为leader）。**

## zk集群搭建

首先准备三份解压文件，每一份中都新建一个叫`data`的文件夹：里面新建一个叫做myid的文件，第一个写1，后面递增。

每一份中配置文件改为`zoo.cfg`。zk1对应的zoo.cfg:


```properties
# The number of milliseconds of each tick 心跳检测时间
tickTime=2000
# The number of ticks that the initial 
# synchronization phase can take
# 集群启动后，相互连接，如果在initLimit*tickTime时间内没有连接成功，那么认为连接失败
initLimit=10
# The number of ticks that can pass between 
# sending a request and getting an acknowledgement
# Masrer和slave之间或者slave和slave之间的数据同步时间，在syncLimit*tickTime是按内没有返回一个ACk，则
# 认为该节点宕机，如果是Master宕机了，就要重新选举了
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just 
# example sakes.
# 数据存放的目录，这是我新建的
dataDir=D:/zookeeper1-3.4.10/data
# the port at which the clients will connect
# 客户端连接集群的端口号
clientPort=2181
# the maximum number of client connections.
# increase this if you need to handle more clients
#maxClientCnxns=60
#
# Be sure to read the maintenance section of the 
# administrator guide before turning on autopurge.
#
# http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
#
# The number of snapshots to retain in dataDir
#autopurge.snapRetainCount=3
# Purge task interval in hours
# Set to "0" to disable auto purge feature
#autopurge.purgeInterval=1


# 第一个端口是数据同步的端口号  第二个端口是选举的端口号
server.1=127.0.0.1:2887:3887
server.2=127.0.0.1:2888:3888
server.3=127.0.0.1:2889:3889
```

后面一次递增这个`clientPort`和`data`文件夹位置。

依次启动即可。