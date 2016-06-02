package me.zhangxl.antenna.infrastructure.frame_process;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;


/**
 * 这里指定一个原则:如果在不恰当的阶段收到了一个完全不相关的桢
 * 则设置这个桢对应的nav。
 * 原来的原则是直接ignore。
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
