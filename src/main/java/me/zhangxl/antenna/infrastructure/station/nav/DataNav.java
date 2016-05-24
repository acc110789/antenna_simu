package me.zhangxl.antenna.infrastructure.station.nav;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * 一个节点收到了DataFrame,但是这个DataFrame
 * 的targetId不是自己,这时候需要设置nav
 * Created by zhangxiaolong on 16/5/24.
 */
public class DataNav extends AbstractNav {

    public DataNav(Station station) {
        super(station);
    }

    @Override
    double getNavDuration() {
        return PrecisionUtil.add(Config.getInstance().getSifs(),
                DataFrame.getFrameTimeLength(),
                Config.getInstance().getDifs());
    }
}
