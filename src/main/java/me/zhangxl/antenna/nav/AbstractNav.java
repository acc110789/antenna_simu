package me.zhangxl.antenna.nav;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
public abstract class AbstractNav implements Navable{
    final Station station;
    AbstractNav(Station station){
        this.station = station;
    }

    @Override
    public void startNav(){
        station.setCurrentStatus(Status.NAV);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
            }
        },getNavDuration(), TimeTask.SEND);
    }

    abstract double getNavDuration();
}
