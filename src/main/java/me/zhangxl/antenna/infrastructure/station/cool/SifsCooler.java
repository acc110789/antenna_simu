package me.zhangxl.antenna.infrastructure.station.cool;

import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/5/24.
 */
public class SifsCooler extends AbstractCooler {
    public SifsCooler(Station station) {
        super(station);
    }

    @Override
    double getCoolDuration() {
        return Config.getInstance().getSifs();
    }
}
