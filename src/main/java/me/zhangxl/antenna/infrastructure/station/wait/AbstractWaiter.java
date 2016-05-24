package me.zhangxl.antenna.infrastructure.station.wait;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;
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

    public AbstractWaiter(Station station){
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
                    station.setCommunicationTarget(Station.defaultCommunicationTarget);
                    station.setCurrentStatus(Status.SLOTING);
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