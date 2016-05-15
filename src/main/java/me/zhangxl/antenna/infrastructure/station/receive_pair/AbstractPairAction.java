package me.zhangxl.antenna.infrastructure.station.receive_pair;

import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * Created by zhangxiaolong on 16/5/15.
 */
public abstract class AbstractPairAction implements PairAction{
    final Station station;
    public AbstractPairAction(Station station){
        this.station = station;
    }
}
