### Antenna Simulation

本程序用于无线局域网MAC层网路包传输过程仿真,另有`Smpl`程序也可以实现无线局域网络的MAC层仿真,
但`Smpl`基于面向过程采用事件分发的方式编程,整个仿真过程晦涩不易看懂.

本程序采用面向对象的方式重新设计程序.整个仿真过程跟真实场景的过程基本一致.

### 实现

本分支采用PCP节点来管理无线网络。采用PCP节点的该技术有以下几个要点。

1. 网中所有节点的状态信息都存储在PCP中,当一个节点想向另一个节点通信的时候,
需要先询问PCP对方是否繁忙,在得到PCP节点允许的情况下,两个节点才能进行通信.
2. PCP允许两个节点进行通信的时候,需要给两个节点分配一个信道,两个节点在该
信道中进行通信.
3. 关于NextRoundFrame有两点说明。
4. 新增加NavFrame来通知某个节点设置Nav。
5. 新增一个点,当两个节点使用同一个频率向Pcp节点发送RTS的时候,Pcp的NextRoundFrame
允许的slot数量限制将设置为0.
6. 在NextRoundFrame增加一个'清空'标志位,配合slot数量限制用来表明,所以Frame的
窗口值加倍。

总结两种节点(普通节点和PCP节点)的行为。<br/>
对于PCP节点来说,其生命周期内的工作方式是这样的:
发送NextRoundFrame,然后等待RTS,如果超时(在一定时间内,没有收到任何RTS)的话
则继续发送NextRoundFrame。如果没有超时,收到了信号(RTS),则将相应的操作
处理完毕之后再发送NextRoundFrame。<br/>
而对于普通节点来说,则是不断的等待NextRoundFrame,根据NextRoundFrame指定的
slot数量进行退避,如果在指定的slot数量之内,要发送的帧的退避窗口值退避到0,则
立刻发送RTS,如果在指定的slot之内窗口值没有退避到0,则冷冻退避窗口,继续等待下一个
NextRoundFrame

### 2016.6.22新的问题

Pcp节点给周围的节点传输信息时,考虑增加几种帧类型。

### 参考
> [802.11MAC](http://wenku.baidu.com/view/37438269561252d380eb6ea8.html?re=view)<br/>
> [802.11MAC简介(二)](http://blog.csdn.net/leonsc/article/details/5719068)<br/>
> [802.11 Lecture2.pdf](http://www.cs.tut.fi/kurssit/TLT-6556/Slides/Lecture2.pdf)<br/>
<br/>
> LAN/MAN standards Committee. Part 11: Wireless lan medium access
 control (mac) and physical layer (phy) specifications[J].
 IEEE-SA Standards Board, 2012.