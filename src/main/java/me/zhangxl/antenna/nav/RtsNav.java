package me.zhangxl.antenna.nav;

import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.Station;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
public class RtsNav extends AbstractNav {

    public RtsNav(Station station) {
        super(station);
    }

    @Override
    double getNavDuration() {
        return RtsFrame.getStaticNavDuration();
    }
}
