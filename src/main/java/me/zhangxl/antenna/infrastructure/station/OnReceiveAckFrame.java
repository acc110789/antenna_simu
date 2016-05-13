package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceiveAckFrame extends OnReceiveFrameLogic {
    public OnReceiveAckFrame(Station station) {
        super(station);
    }

    @Override
    void doLogic(final Frame frame) {
        assert frame instanceof AckFrame;
        assert station.getCurrentStatus() == Status.WAITING_ACK;
        station.setCurrentStatus(Status.RECEIVING_ACK);

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                assert station.getCurrentStatus() == Status.RECEIVING_ACK;
                station.receivingFrames.remove(frame);
                station.mSender.onPostRecvACK((AckFrame) frame);
            }
        }, frame.getEndDuration());
    }
}
