package me.zhangxl.antenna.infrastructure.timeout;

import me.zhangxl.antenna.infrastructure.PostDifsLogic;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import static me.zhangxl.antenna.infrastructure.base.BaseRole.defaultCommunicationTarget;

/**
 * 抽象的超时等待器
 * Created by zhangxiaolong on 16/6/1.
 */
abstract class AbstractWaiter implements Waiter {
    final Logger logger = SimuLoggerManager.getLogger("waiter");
    final Station station;
    AbstractWaiter(Station station){
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
                    //timeout 玩了之后立刻进入下一个时间周期
                    if(isSender()){
                        station.onFail();
                    }
                    station.setCommunicationTarget(defaultCommunicationTarget);
                    station.setCurrentStatus(Status.SLOTING);
                    new PostDifsLogic(station).process();
                }
            }
        },getWaitDuration());
    }

    abstract Status getWaitStatus();

    abstract double getWaitDuration();

    String getInfoToLog(){
        return null;
    }

    abstract boolean isSender();
}
