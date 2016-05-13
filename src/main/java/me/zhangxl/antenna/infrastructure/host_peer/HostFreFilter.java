package me.zhangxl.antenna.infrastructure.host_peer;

import me.zhangxl.antenna.infrastructure.ChannelManager;
import me.zhangxl.antenna.infrastructure.FreFilter;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
class HostFreFilter implements FreFilter {

    @Override
    public boolean canReceive(int pre) {
        return ChannelManager.getInstance().isRtsChannel(pre);
    }
}
