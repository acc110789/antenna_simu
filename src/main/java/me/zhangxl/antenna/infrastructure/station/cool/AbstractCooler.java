package me.zhangxl.antenna.infrastructure.station.cool;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * Created by zhangxiaolong on 16/5/23.
 */
public abstract class AbstractCooler implements Cooler {
    final Station station;

    public AbstractCooler(Station station){
        this.station = station;
    }

    /**
     * 冷却完毕之后,使状态回到SLOTING,然后执行
     * {@link Station#onPostDIFS()}
     */
    public void cool(){
        toRunBeforeCool();
        station.setCurrentStatus(Status.COOLING);
        station.setCommunicationTarget(Station.defaultCommunicationTarget);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(station.getCurrentStatus() == Status.COOLING){
                    station.setCurrentStatus(Status.SLOTING);
                    station.onPostDIFS();
                }
            }
        },getCoolDuration(), TimeTask.SEND);
    }

    abstract double getCoolDuration();

    void toRunBeforeCool(){}
}
