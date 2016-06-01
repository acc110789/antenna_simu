package me.zhangxl.antenna.frame_process;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;


/**
 * Created by zhangxiaolong on 16/6/1.
 */
abstract class AbstractProcessor implements Processor {
    final static Logger  logger = SimuLoggerManager.getLogger("process");
    final Station station;
    AbstractProcessor(Station station){
        this.station = station;
    }

    void sendFrame(Frame frame){
        try {
            Medium.getInstance().putFrame(station, (Frame) frame.clone());
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
