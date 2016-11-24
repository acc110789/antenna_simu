package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.infrastructure.base.ChannelManager;
import me.zhangxl.antenna.infrastructure.base.FreFilter;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public class StationFreFilter implements FreFilter {

    //初始时刻只接收Pcp节点所在的信号频率
    private int fre = ChannelManager.getPcpChannel();

    public void setFre(int fre){
        this.fre = fre;
    }

    int getFre(){
        return this.fre;
    }

    @Override
    public boolean canReceive(int fre) {
        return this.fre == fre;
    }
}
