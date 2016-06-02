package me.zhangxl.antenna.infrastructure.nav;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.util.Constant;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
public class RtsNav extends AbstractNav {

    public RtsNav(Station station) {
        super(station);
    }

    @Override
    double getNavDuration() {
        return Constant.getRtsNav();
    }
}
