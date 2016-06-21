package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.frame_process.AckProcessor;

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
        assert station.getCurrentStatus() == Status.WAITING_ACK;
        station.setCurrentStatus(Status.RECEIVING_ACK);
    }

    @Override
    void onPost() {
        assert station.getCurrentStatus() == Status.RECEIVING_ACK;
        station.getReceivingFrames().remove(frame);
        new AckProcessor(station).process(frame);
    }
}
