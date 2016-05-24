package me.zhangxl.antenna.infrastructure.station.cool;

import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.Config;

/**
 * 收到莫名其妙的东西
 * Created by zhangxiaolong on 16/5/23.
 */
public class EifsCooler extends AbstractCooler {

    public EifsCooler(Station station) {
        super(station);
    }

    @Override
    double getCoolDuration() {
        return Config.getInstance().getEifs();
    }
}
