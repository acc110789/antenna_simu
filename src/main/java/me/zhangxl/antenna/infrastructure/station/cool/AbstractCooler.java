package me.zhangxl.antenna.infrastructure.station.cool;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.Stateful;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.PrecisionUtil;

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
        station.setLastCoolingTimeToNow();
        station.setCurrentStatus(Stateful.Status.COOLING);
        station.setCommunicationTarget(Station.defaultCommunicationTarget);
        if(station.isSender()) {
            station.backOffDueToTimeout();
            station.setReceiver();
        }
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(station.getCurrentStatus() == Stateful.Status.COOLING
                        && isCoolingNotInterrupted()){
                    station.setCurrentStatus(Stateful.Status.SLOTING);
                    station.onPostDIFS();
                }
            }
        },getCoolDuration(), TimeTask.SEND);
    }

    abstract double getCoolDuration();

    void toRunBeforeCool(){}

    /**
     * @return 这个cooling的过程没有被打断
     */
    private boolean isCoolingNotInterrupted(){
        return PrecisionUtil.equal(getCoolDuration(),
                PrecisionUtil.sub(TimeController.getInstance().getCurrentTime(),
                        station.getLastCoolingTime())
        );
    }
}
