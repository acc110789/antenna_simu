package me.zhangxl.antenna.infrastructure.station.cool;

import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * Created by zhangxiaolong on 16/5/23.
 */
public class SenderDifsCooler extends DifsCooler {

    public SenderDifsCooler(Station station) {
        super(station);
    }

    @Override
    void toRunBeforeCool() {
        //作为send需要进行统计的数据
        station.onSendSuccess();
    }
}
