package me.zhangxl.antenna.infrastructure.timeout;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.util.Constant;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
public class WaitAckTimeOut extends AbstractWaiter {

    public WaitAckTimeOut(Station station) {
        super(station);
    }

    @Override
    Status getWaitStatus() {
        return Status.WAITING_DACK;
    }

    @Override
    double getWaitDuration() {
        return Constant.getAckTimeOut();
    }

    @Override
    boolean isSender() {
        return true;
    }

    @Override
    String getInfoToLog() {
        return String.format("%d after onPostSendDATA(),wait ack timeout", station.getId());
    }
}
