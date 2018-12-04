# Zookeeper笔记5-ZAB协议

## 一、前言

ZooKeeper是一个分布式协调服务，可用于服务发现、分布式锁、分布式领导选举、配置管理等。
 
**这一切的基础，都是ZooKeeper提供了一个类似于Linux文件系统的树形结构（可认为是轻量级的内存文件系统，但只适合存少量信息，完全不适合存储大量文件或者大文件），同时提供了对于每个节点的监控与通知机制。**
 
既然是一个文件系统，就不得不提ZooKeeper是如何保证数据的一致性的。本节将将介绍ZooKeeper如何保证数据一致性，如何进行领导选举，以及数据监控/通知机制的语义保证。

## 二、ZAB-原子广播（重点）

ZooKeeper集群是一个基于主从复制的高可用集群，每个服务器承担如下三种角色中的一种：
- ==Leader== 一个ZooKeeper集群同一时间只会有一个实际工作的Leader，它会发起并维护与各Follwer及Observer间的心跳。所有的写操作必须要通过Leader完成再由Leader将写操作广播给其它服务器。
- ==Follower== 一个ZooKeeper集群可能同时存在多个Follower，它会响应Leader的心跳。Follower可直接处理并返回客户端的读请求，同时会将写请求转发给Leader处理，并且负责在Leader处理写请求时对请求进行投票。
- ==Observer== 角色与Follower类似，但是无投票权。

<div align="center">
    <img src="../pic/zookeeper/ZAB协议-1.jpg" >
</div>

==为了保证写操作的一致性与可用性，ZooKeeper专门设计了一种名为原子广播（ZAB）的支持崩溃恢复的一致性协议。基于该协议，ZooKeeper实现了一种主从模式的系统架构来保持集群中各个副本之间的数据一致性。==
 
根据ZAB协议，所有的写操作都必须通过Leader完成，Leader写入本地日志后再复制到所有的Follower节点。
 
一旦Leader节点无法工作，ZAB协议能够自动从Follower节点中重新选出一个合适的替代者，即新的Leader，该过程即为领导选举。该领导选举过程，是ZAB协议中最为重要和复杂的过程。

### 1、写Leader
 
通过Leader进行写操作流程如下图所示：

<div align="center">
    <img src="../pic/zookeeper/ZAB协议-2.jpg" >
</div>
由上图可见，通过Leader进行写操作，主要分为五步：
 
- 客户端向Leader发起写请求
- Leader将写请求以Proposal的形式发给所有Follower并等待ACK
- Follower收到Leader的Proposal后返回ACK
- Leader得到过半数的ACK（Leader对自己默认有一个ACK）后向所有的Follower和Observer发送Commmit
- Leader将处理结果返回给客户端

==这里要注意：==

- Leader并不需要得到Observer的ACK，即Observer无投票权
- Leader不需要得到所有Follower的ACK，只要收到过半的ACK即可，同时Leader本身对自己有一个ACK。上图中有4个Follower，只需其中两个返回ACK即可，因为(2+1) / (4+1) > 1/2
- Observer虽然无投票权，但仍须同步Leader的数据从而在处理读请求时可以返回尽可能新的数据


### 2、写Follower/Observer
 
通过Follower/Observer进行写操作流程如下图所示：

<div align="center">
    <img src="../pic/zookeeper/ZAB协议-3.jpg" >
</div>

从上图可见：

- Follower/Observer均可接受写请求，但不能直接处理，而需要将写请求转发给Leader处理
- 除了多了一步请求转发，其它流程与直接写Leader无任何区别

## 3、读操作
 
Leader/Follower/Observer都可直接处理读请求，从本地内存中读取数据并返回给客户端即可。

<div align="center">
    <img src="../pic/zookeeper/ZAB协议-4.jpg" >
</div>

由于处理读请求不需要服务器之间的交互，Follower/Observer越多，整体可处理的读请求量越大，也即读性能越好。

==在整个消息广播过程中，Leader服务器会为每个事务请求生成对应的Proposal来进行广播，并且在广播事务Proposal之前，Leader服务器会首先为这个事务Proposal分配一个全局单调递增的唯一ID，我们称之为事务ID(即ZXID)。由于ZAB协议需要保证每一个消息严格的因果关系，因此必须将每一个事务Proposal按照其ZXID的先后顺序进行排序和处理。==

具体的，在消息广播过程中，Leader服务器会为每个Follower服务器都==各自分配一个单独的队列==，然后将需要广播的事务Proposal依次放入这些队列中取，并且根据FIFO策略进行消息发送。每一个Follower服务器在接收到这个事务Proposal之后，都会首先将其以事务日志的形式写入本地磁盘中，并且成功写入后反馈给Leader服务器一个Ack相应。当Leader服务器接收到过半数Follower的Ack响应后，就会广播一个Commit消息给所有的Follower服务器以通知其进行事务提交，同时Leader自身也会完成对事务的提交，而每个Follower服务器在接收到Commit消息后，也会完成对事务的提交。


==然而，在这种简化的二阶段提交模型下，无法处理Leader服务器崩溃退出而带来的数据不一致问题，因此ZAB协议添加了崩溃恢复模式来解决这个问题==。另外，整个消息广播协议是基于有FIFO特性的TCP协议来进行网络通信的，因此很容易地保证消息广播过程中消息接收和发送的顺序性。

在ZAB协议中，为了保证程序的正确运行，整个恢复过程结束后需要选举出一个新的Leader服务器。因此，ZAB协议需要一个高效且可靠的Leader选举算法，从而确保能够快速选举出新的Leader。同时，Leader选举算法不仅仅需要让Leader自己知道其自身已经被选举为Leader，同时还需要让集群中的所有其他服务器也快速地感知到选举产生的新的Leader服务器。==崩溃恢复主要包括Leader选举和数据恢复两部分==，下面将详细讲解Leader选举和数据恢复流程。



## 三、支持的领导选举算法

在3.4.10版本中，默认值为3，也即基于TCP的FastLeaderElection。另外三种算法已经被弃用，并且有计划在之后的版本中将它们彻底删除而不再支持。

何时触发选举？

选举Leader不是随时选举的，毕竟选举有产生大量的通信，造成网络IO的消耗。因此下面情况才会出现选举：

- 集群启动
- 服务器处于寻找Leader状态
- 当服务器处于LOOKING状态时，表示当前没有Leader，需要进入选举流程
- 崩溃恢复
- Leader宕机
- 网络原因导致过半节点与Leader心跳中断

下面学习一下FastLeaderElection的原理。


## 四、名词解释

### 1、myid
 
每个ZooKeeper服务器，都需要在数据文件夹下创建一个名为myid的文件，该文件包含整个ZooKeeper集群唯一的ID（整数）。例如，某ZooKeeper集群包含三台服务器，hostname分别为zoo1、zoo2和zoo3，其myid分别为1、2和3，则在配置文件中其ID与hostname必须一一对应，如下所示。在该配置文件中，server.后面的数据即为myid


```
server.1=zoo1:2888:3888
server.2=zoo2:2888:3888
server.3=zoo3:2888:3888
```

- 第1个端口是通信和数据同步端口，默认是2888
- 第2个端口是投票端口，默认是3888

数小的向数大的发起TCP连接。比如有3个节点，myid文件内容分别为1,2,3。zk集群的tcp连接顺序是1向2发起TCP连接，2向3发起TCP连接。如果有n个节点，那么tcp连接顺序也以此类推。这样整个zk集群就会连接起来

### 2、zxid
 
类似于RDBMS中的事务ID，用于标识一次更新操作的Proposal ID。为了保证顺序性，该zxid必须==单调递增==。因此ZooKeeper使用一个64位的数来表示，**高32位是Leader的epoch，从1开始，每次选出新的Leader，epoch加一。低32位为该epoch内的序号，每次epoch变化，**都将低32位的序号重置****。这样保证了zxid的全局递增性。

### 3、服务器状态
 
- LOOKING 不确定Leader状态。该状态下的服务器认为当前集群中没有Leader，会发起Leader选举。
- FOLLOWING 跟随者状态。表明当前服务器角色是Follower，并且它知道Leader是谁。
- LEADING 领导者状态。表明当前服务器角色是Leader，它会维护与Follower间的心跳。
- OBSERVING 观察者状态。表明当前服务器角色是Observer，与Folower唯一的不同在于不参与选举，也不参与集群写操作时的投票。

### 4、选票数据结构


每个服务器在进行领导选举时，会发送如下关键信息：

- logicClock 每个服务器会维护一个自增的整数，名为logicClock，它表示这是该服务器发起的第多少轮投票
- state 当前服务器的状态
- self_id 当前服务器的myid
- self_zxid 当前服务器上所保存的数据的最大zxid
- vote_id 被推举的服务器的myid
- vote_zxid 被推举的服务器上所保存的数据的最大zxid


## 五、leader的判定标准

- 数据新旧程度，只有拥有最新数据的节点才能有机会成为Leader，通过zxid的大小来表示数据的新，zxid越大代表数据越新
- myid:集群启动时，会在data目录下配置myid文件，里面的数字代表当前zk服务器节点的编号.当zk服务器节点数据一样新时， myid中数字越大的就会被选举成Leader
- 当集群中已经有Leader时，新加入的节点不会影响原来的集群
- 投票数量，只有得到集群中多半的投票，才能成为Leader，多半即：n/2+1,其中n为集群中的节点数量




## 六、Leader选举流程

### 1、自增选举轮次

ZooKeeper规定所有有效的投票都必须在同一轮次中。每个服务器在开始新一轮投票时，会先对自己维护的logicClock进行自增操作。

### 2、发送初始化选票

每个服务器最开始都是通过广播把票投给自己。

### 4、更新选票

根据选票logicClock -> vote_zxid -> vote_id依次判断

#### 4.1 判断选举轮次收到外部投票后，首先会根据投票信息中所包含的logicClock来进行不同处理：

==**外部投票的logicClock > 自己的logicCloc**k==:

说明该服务器的选举轮次落后于其它服务器的选举轮次，立即清空自己的投票箱并将自己的logicClock更新为收到的logicClock，然后再对比自己之前的投票与收到的投票以确定是否需要变更自己的投票，最终再次将自己的投票广播出去;

==**外部投票的logicClock < 自己的logicClock:**== 当前服务器直接忽略该投票，继续处理下一个投票;

==**外部投票的logickClock = 自己的:**== 进行选票PK。


#### 4.2 选票PK选票PK是基于(self_id, self_zxid)与(vote_id, vote_zxid)的对比：

若logicClock一致，则对比二者的vote_zxid。

==若外部投票的vote_zxid比较大==，则将自己的票中的vote_zxid与vote_myid更新为收到的票中的vote_zxid与vote_myid并广播出去，==另外将收到的票及自己更新后的票放入自己的票箱==。如果票箱内已存在(self_myid, self_zxid)相同的选票，则直接覆盖

若二者vote_zxid一致，则比较二者的vote_myid。

==若外部投票的vote_myid比较大==，则将自己的票中的vote_myid更新为收到的票中的vote_myid并广播出去，另外将收到的票及自己更新后的票放入自己的票箱


### 5、统计选票

如果已经确定有过半服务器认可了自己的投票（可能是更新后的投票），则终止投票。否则继续接收其它服务器的投票。

### 6、更新服务器状态

投票终止后，服务器开始更新自身状态。若过半的票投给了自己，则将自己的服务器状态更新为LEADING，否则将自己的状态更新为FOLLOWING。


## 七、图示Leader选举流程

<div align="center">
    <img src="../pic/zookeeper/ZAB协议-5.jpg" >
</div>

说明：

图中箭头上的(1,1,0) 三个数依次代表

- 该选票的服务器的LogicClock（即投票轮数）;
- 被推荐的服务器的myid (即vote_myid);
- 被推荐的服务器的最大事务ID(即vote_zxid)；

所以(1,1,0)在这里的意思是：第一轮投票中，投给server 1，并且自己的最大事务ID都是0(==这里可能会比较乱==，ZXID可用这样理解：前32位是年号，比如万历年间；后32位是多少年，比如万历15年)，我们这里先不考虑年号的更迭，就假设这个投票发生在万历15年这一年，并且只考虑第一轮投票。即(1,vote_id,0)，所以暂时只考虑中间个数字。后面接受外部选票的时候，我们只要关注中间个数字即可，比如(1,2,0)说明是投给server 2的。

这里的示例只考虑第一轮，并且ZXID就是0.

(1, 1)表示：

- 投票服务器myid(即self_myid)
- 被推荐的服务器的myid (即vote_myid)


### 第一步：自增选票轮次&初始化选票&发送初始化选票

首先，三台服务器自增选举轮次将LogicClock=1；然后初始化选票，清空票箱；最后发起初始化投票给自己将各自的票通过广播的形式投个自己并保存在自己的票箱里。

所以都是自己投给自己一票(1,1,0),(1,2,0),(1,3,0)

投完票之后的状态时(1,1),(2,2),(3,3)

### 第二步：接受外部投票&更新选票

以Server 1 为例，分别经历 Server 1 PK Server 2 和 Server 1 PK Server 3 过程

#### Server 1 PK Server 2

Server 1 接收到Server 2的选票(1,2,0) 表示投给server2.

这时Server 1将自身的选票轮次和Server 2 的选票轮次比较，发现LogicClock=1相等，接着Server 2比较比较最大事务ID，发现也zxid=0也相等，最后比较各自的myid，发现Server 2的myid=2 大于自己的myid=1；

根据选票PK规则，Server 1将自己的选票由 (1, 1) 更正为 (1, 2)，表示选举Server 2为Leader，然后将自己的新选票 (1, 2)广播给 Server 2 和 Server 3，同时更新票箱子中自己的选票并保存Server 2的选票，至此Server 1票箱中的选票为(1, 2) 和 (2, 2)；

Server 2收到Server 1的选票同样经过轮次比较和选票PK后确认自己的选票保持不变，并更新票箱中Server 1的选票由(1, 1)更新为(1, 2)，注意此次Server 2自己的选票并没有改变所有不用对外广播自己的选票。

此时便认为已经选出了Leader。

#### Server 1 PK Server 3

Server 1 接收到Server 3的选票(1,3,0) 表示投给server3.

根据Server 1 PK Server 2的流程类推，Server 1自己的选票由(1, 2)更新为(1, 3), 同样更新自己的票箱并广播给Server 2 和 Server 3；

Server 2再次接收到Server 1的选票(1, 3)时经过比较后根据规则也要将自己的选票从(1, 2)更新为(1, 3), 并更新票箱里自己的选票和Server 1的选票，同时向Server 1和 Server 3广播；

同理 Server 2 和 Server 3也会经历上述投票过程，依次类推，Server 1 、Server 2 和Server 3 在俩俩之间在经历多次选举轮次比较和选票PK后最终确定各自的选票。

最后更新服务器状态：

<div align="center">
    <img src="../pic/zookeeper/ZAB协议-6.jpg" >
</div>

选票确定后服务器根据自己票箱中的选票确定各自的角色和状态，票箱中超过半数的选票投给自己的则为Leader，更新自己的状态为LEADING，否则为Follower角色，状态为FOLLOWING，成为Leader的服务器要主动向Follower发送心跳包，Follower做出ACK回应，以维持他们之间的长连接。


- https://dbaplus.cn/news-141-1875-1.html
- https://www.jianshu.com/p/3fec1f8bfc5f