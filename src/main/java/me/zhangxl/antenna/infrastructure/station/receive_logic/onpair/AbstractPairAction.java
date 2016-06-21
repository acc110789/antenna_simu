package me.zhangxl.antenna.infrastructure.station.receive_logic.onpair;


import me.zhangxl.antenna.infrastructure.Station;

/**
 * Created by zhangxiaolong on 16/5/15.
 */
public abstract class AbstractPairAction implements PairAction{
    final Station station;
    public AbstractPairAction(Station station){
        this.station = station;
    }
}
