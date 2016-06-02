package me.zhangxl.antenna.nav;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.util.Constant;

/**
 * Created by zhangxiaolong on 16/6/2.
 */
public class CtsNav extends AbstractNav {

    public CtsNav(Station station) {
        super(station);
    }

    @Override
    double getNavDuration() {
        return Constant.getCtsNav();
    }
}
