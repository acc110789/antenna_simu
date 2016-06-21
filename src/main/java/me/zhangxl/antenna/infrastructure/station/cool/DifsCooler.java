package me.zhangxl.antenna.infrastructure.station.cool;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/5/25.
 */
public class DifsCooler extends AbstractCooler {

    public DifsCooler(Station station) {
        super(station);
    }

    @Override
    double getCoolDuration() {
        return Config.getInstance().getDifs();
    }
}
