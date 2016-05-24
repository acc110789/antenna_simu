package me.zhangxl.antenna.infrastructure.station.wait;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * 发送方等待ack超时
 * Created by zhangxiaolong on 16/5/23.
 */
public class AckTimeOutWaiter extends AbstractWaiter{

    public AckTimeOutWaiter(Station station) {
        super(station);
    }

    @Override
    double getTimeOut() {
        return AckFrame.getAckTimeOut();
    }

    @Override
    Status getWaitingStatus() {
        return Status.WAITING_ACK;
    }

    @Override
    String getInfoToLog() {
        return String.format("%d after onPostSendDATA(),wait ack timeout",station.getId());
    }
}
