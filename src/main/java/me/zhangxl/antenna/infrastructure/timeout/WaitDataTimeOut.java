package me.zhangxl.antenna.infrastructure.timeout;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.util.Constant;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
public class WaitDataTimeOut extends AbstractWaiter {

    public WaitDataTimeOut(Station station) {
        super(station);
    }

    @Override
    Status getWaitStatus() {
        return Status.WAITING_DATA;
    }

    @Override
    double getWaitDuration() {
        return Constant.getDataTimeOut();
    }

    @Override
    boolean isSender() {
        //是receiver使用的东西,所以返回false
        return false;
    }

    @Override
    String getInfoToLog() {
        return String.format("station :%d after onPostSendCTS(),wait data timeout", station.getId());
    }
}
