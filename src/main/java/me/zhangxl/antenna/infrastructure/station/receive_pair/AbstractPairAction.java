package me.zhangxl.antenna.infrastructure.station.receive_pair;

import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by zhangxiaolong on 16/5/15.
 */
public abstract class AbstractPairAction implements PairAction{
    static final Logger logger = SimuLoggerManager.getLogger("Pair");
    final Station station;
    public AbstractPairAction(Station station){
        this.station = station;
    }
}
