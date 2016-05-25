package me.zhangxl.antenna.infrastructure.station.nav;

import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * 一个节点收到一个完整的RTS之后,发现这个RTS不是
 * 发给自己的,于是需要设置NAV,这种情况下,用本类
 * 来设置nav向量
 * Created by zhangxiaolong on 16/5/24.
 */
public class RtsNav extends AbstractNav {

    public RtsNav(Station station) {
        super(station);
    }

    @Override
    double getNavDuration() {
        return RtsFrame.getRtsNavDuration();
    }

    @Override
    String getInfoToLog() {
        return "receive a unknown RtsFrame";
    }
}
