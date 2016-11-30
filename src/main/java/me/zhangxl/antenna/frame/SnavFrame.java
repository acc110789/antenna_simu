package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

/**
 * 这个Frame是新定义的Frame。这个Frame的使用场景如下:
 * 当一个节点A想要给另外一个节点B发送DataFrame的时候,它先向Pcp
 * 发送RTS,但是Pcp发现节点B此时正处于繁忙的状态,于是Pcp向节点A
 * 回复一个NavFrame,要求B设置Nav直到B节点通信结束,然后A再次等待
 * BofFrame之后在此向节点B进行通信的请求过程
 *
 * NavFrame暂时设置为两个地址,且src和target的地址都是节点A的地址
 *
 * NavFrame设置两个地址吧。
 * Created by zhangxiaolong on 16/6/29.
 */
public class SnavFrame extends Frame {

    //暂时认为nav的长度和RtsFrame的长度一致
    private static final int navFrameLength = Config.getRtsLength();

    private double navDuration = -1;
    //需要注意的是如果srcId和targetId都是-1,则代表所有的节点都需要设置nav
    public SnavFrame(int srcId, int targetId, int fre) {
        super(srcId, targetId, navFrameLength, fre);
    }

    public void setNavDuration(double navDuration){
        this.navDuration = navDuration;
    }

    public double getNavDuration(){
        return this.navDuration;
    }
}
