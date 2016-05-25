package me.zhangxl.antenna.infrastructure.station.nav;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.station.cool.DifsCooler;
import me.zhangxl.antenna.infrastructure.station.wait.AbstractWaiter;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

/**
 * Created by zhangxiaolong on 16/5/24.
 */
abstract class AbstractNav implements NavTimer {
    private static final Logger logger = SimuLoggerManager.getLogger(AbstractWaiter.class.getSimpleName());
    final Station station;

    AbstractNav(Station station) {
        this.station = station;
    }

    @Override
    public void startNav(){
        if(station.isSender()){
            station.backOffDueToTimeout();
            station.setReceiver();
        }
        station.setCurrentStatus(Status.NAVING);

        String format = "%d ";
        if(!StringUtils.isEmpty(getInfoToLog())){
            format += getInfoToLog();
        }
        format += " setNAV";
        logger.info(format,station.getId());

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                logger.info("%d NAV finish",station.getId());
                new DifsCooler(station).cool();
            }
        },getNavDuration(), TimeTask.SEND);
    }

    abstract double getNavDuration();

    String getInfoToLog(){
        return null;
    }
}
