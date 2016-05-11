package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.base.Stateful;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceiveAckFrame extends OnReceiveFrameLogic {

    public OnReceiveAckFrame(Station station, Frame frame) {
        super(station, frame);
    }

    @Override
    void onPre() {
        assert frame instanceof AckFrame;
        assert station.getCurrentStatus() == Stateful.Status.WAITING_ACK;
        station.setCurrentStatus(Stateful.Status.RECEIVING_ACK);
    }

    @Override
    void onPost() {
        assert station.getCurrentStatus() == Stateful.Status.RECEIVING_ACK;
        station.getReceivingFrames().remove(frame);
        station.getSender().onPostRecvACK((AckFrame) frame);
    }
}
