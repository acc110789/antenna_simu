package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceiveAckFrame extends OnReceiveFrameLogic {


    public OnReceiveAckFrame(Station station, Frame frame) {
        super(station, frame);
    }

    @Override
    void onPreFrame() {
        assert frame instanceof AckFrame;
    }

    @Override
    public void onClearFrame() {
        assert station.getCurrentStatus() == Status.WAITING_ACK;
        station.receivingFrames.remove(frame);
        station.mSender.onPostRecvACK((AckFrame) frame);
    }
}
