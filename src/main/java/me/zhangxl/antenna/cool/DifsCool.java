package me.zhangxl.antenna.cool;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
public class DifsCool extends AbstractCool {
    DifsCool(Station station) {
        super(station);
    }

    @Override
    double getCoolDuration() {
        return Config.getInstance().getDifs();
    }
}
