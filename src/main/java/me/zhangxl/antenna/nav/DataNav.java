package me.zhangxl.antenna.nav;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Constant;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * Created by zhangxiaolong on 16/6/2.
 */
public class DataNav extends AbstractNav {
    public DataNav(Station station) {
        super(station);
    }

    @Override
    double getNavDuration() {
        return PrecisionUtil.add(Config.getInstance().getSifs(),
                Constant.getAckTimeLength());
    }
}
