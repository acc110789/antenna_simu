package me.zhangxl.antenna.infrastructure.station.cool;

import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.Config;

/**
 * 一次成功的接受或者发送之后需要等待长达difs在开始backoff
 * Created by zhangxiaolong on 16/5/23.
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
