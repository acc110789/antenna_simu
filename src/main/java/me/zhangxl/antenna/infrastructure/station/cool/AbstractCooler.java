package me.zhangxl.antenna.infrastructure.station.cool;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.BaseRole;
import me.zhangxl.antenna.infrastructure.base.ChannelManager;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * Created by zhangxiaolong on 16/5/25.
 */
abstract class AbstractCooler implements Cooler {

    final Station station;

    AbstractCooler(Station station) {
        this.station = station;
    }

    @Override
    public void cool() {
        station.setCurrentStatus(Status.COOLING);
        station.setCoolTimeNow();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (station.getCurrentStatus() == Status.COOLING &&
                        isCoolingNotInterrupted()) {
                    station.setCurrentStatus(Status.WAITING_BACK_OFF);
                    station.setCommunicationTarget(BaseRole.defaultCommunicationTarget);
                    station.setAcceptFre(ChannelManager.getPcpChannel());
                } else {
                    throw new IllegalStateException("impossible to be interrupted");
                }
            }
        }, getCoolDuration());
    }

    abstract double getCoolDuration();

    private boolean isCoolingNotInterrupted() {
        double value1 = getCoolDuration();
        double value2 = PrecisionUtil.sub(TimeController.getInstance().getCurrentTime(),
                station.getLastCoolingTime());
        return PrecisionUtil.equal(value1, value2);
    }
}
