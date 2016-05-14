package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.infrastructure.ChannelManager;
import me.zhangxl.antenna.infrastructure.FreFilter;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
class StationFreFilter implements FreFilter {

    private int fre = ChannelManager.getInstance().getPcpChannel();

    void setFre(int fre){
        this.fre = fre;
    }

    @Override
    public boolean canReceive(int fre) {
        return this.fre == fre;
    }
}