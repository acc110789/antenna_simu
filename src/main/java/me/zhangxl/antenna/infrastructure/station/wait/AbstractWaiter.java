package me.zhangxl.antenna.infrastructure.station.wait;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.Stateful.Status;
import me.zhangxl.antenna.infrastructure.station.Stateful;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

/**
 * Created by zhangxiaolong on 16/5/23.
 */
public abstract class AbstractWaiter implements Waiter {
    private static final Logger logger = SimuLoggerManager.getLogger(AbstractWaiter.class.getSimpleName());
    final Station station;

    AbstractWaiter(Station station){
        this.station = station;
    }

    @Override
    public void await(){
        station.setCurrentStatus(getWaitingStatus());
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(station.getCurrentStatus() == getWaitingStatus()){
                    String info = getInfoToLog();
                    if(!StringUtils.isEmpty(info)){
                        logger.info(info);
                    }
                    //timeout 意味着此次的传输失败,如果是作为发送者传输失败,则需要加大竞争窗口值
                    if(station.isSender()){
                        station.backOffDueToTimeout();
                        station.setReceiver();
                    }
                    station.setCommunicationTarget(Station.defaultCommunicationTarget);
                    station.setCurrentStatus(Stateful.Status.SLOTING);
                    station.onPostDIFS();
                }
            }
        }, getTimeOut(), TimeTask.SEND);
    }

    abstract double getTimeOut();

    String getInfoToLog(){
        return null;
    }

    abstract Status getWaitingStatus();

}
