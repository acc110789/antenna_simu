package me.zhangxl.antenna.infrastructure.nav;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
abstract class AbstractNav implements Navable{
    final Logger logger = SimuLoggerManager.getLogger("nav");
    final Station station;
    AbstractNav(Station station){
        this.station = station;
    }

    @Override
    public void startNav(){
        station.setCurrentStatus(Status.NAV);
        logger.info("%d set NAV",station.getId());
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                logger.info("%d NAV finish",station.getId());
                station.setCurrentStatus(Status.WAITING_NEXT_ROUND);
//                new DifsCool(station).cool();
            }
        },getNavDuration(), TimeTask.RECEIVE);
    }

    abstract double getNavDuration();
}
