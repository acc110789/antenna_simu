package me.zhangxl.antenna.infrastructure.pcp;

import me.zhangxl.antenna.infrastructure.base.ChannelManager;
import me.zhangxl.antenna.infrastructure.base.FreFilter;

/**
 * PcpStation的频率过滤器只能接收RTS频率的桢
 * Created by zhangxiaolong on 16/5/13.
 */
class PcpFreFilter implements FreFilter {

    @Override
    public boolean canReceive(int pre) {
        return ChannelManager.isRtsChannel(pre);
    }
}
