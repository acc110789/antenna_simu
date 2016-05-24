package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.station.cool.DifsCooler;

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
        if(frame.getTargetId() == station.getId()) {
            station.mSender.onPostRecvACK((AckFrame) frame);
        } else {
            new DifsCooler(station).cool();
        }
    }
}
