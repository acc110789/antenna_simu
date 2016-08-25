package me.zhangxl.antenna.infrastructure.timeout;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

/**
 * Created by zhangxiaolong on 16/5/26.
 */
abstract class AbstractTimeOut implements Waiter {
    static final Logger logger = SimuLoggerManager.getLogger("waiter");
    final Station station;
    AbstractTimeOut(Station station){
        this.station = station;
    }

    @Override
    public void await() {
        station.setCurrentStatus(getWaitStatus());
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(station.getCurrentStatus() == getWaitStatus()){
                    //说明已经超时了
                    if(!StringUtils.isEmpty(getInfoToLog())){
                        logger.info(getInfoToLog());
                    }
                    station.setCurrentStatus(Status.WAITING_BACK_OFF);
                }
            }
        },getWaitDuration());
    }

    abstract Status getWaitStatus();

    abstract double getWaitDuration();

    String getInfoToLog(){
        return null;
    }
}
