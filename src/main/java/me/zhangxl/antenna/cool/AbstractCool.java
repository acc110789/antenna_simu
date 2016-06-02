package me.zhangxl.antenna.cool;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.PrecisionUtil;

import static me.zhangxl.antenna.infrastructure.base.BaseRole.defaultCommunicationTarget;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
abstract class AbstractCool implements Cooler {
    final Station station;

    AbstractCool(Station station) {
        this.station = station;
    }

    @Override
    public void cool() {
        station.setLastCoolTimeNow();
        station.setCurrentStatus(Status.COOLING);
        station.setCommunicationTarget(defaultCommunicationTarget);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                //在这期间一直处于cooling的状态没有被打断
                if(station.getCurrentStatus() == Status.COOLING &&
                        coolingNotInterrupted()){
                    station.setCurrentStatus(Status.SLOTING);
                    station.onPostDIFS();
                }
            }
        }, getCoolDuration());
    }

    abstract double getCoolDuration();

    private boolean coolingNotInterrupted(){
        double value1 = PrecisionUtil.sub(TimeController.getInstance().getCurrentTime(),
                station.getLastCoolTime());
        return PrecisionUtil.equal(value1,getCoolDuration());
    }
}
