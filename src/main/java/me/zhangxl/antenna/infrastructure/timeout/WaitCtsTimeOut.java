package me.zhangxl.antenna.infrastructure.timeout;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.util.Constant;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
public class WaitCtsTimeOut extends AbstractWaiter {

    public WaitCtsTimeOut(Station station) {
        super(station);
    }

    @Override
    Status getWaitStatus() {
        return Status.WAITING_CTS;
    }

    @Override
    double getWaitDuration() {
        return Constant.getCtsTimeOut();
    }

    @Override
    String getInfoToLog() {
        return String.format("%d after onPostSendRTS() wait CTS timeout",station.getId());
    }

    @Override
    boolean isSender() {
        return true;
    }
}
