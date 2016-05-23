package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
class OnReceiveAckFrame extends OnReceiveFrameLogic {
    OnReceiveAckFrame(Station station) {
        super(station);
    }

    @Override
    public void doLogic(final Frame frame) {
        assert frame instanceof AckFrame;

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(!frame.isDirty()) {
                    assert station.getCurrentStatus() == Status.WAITING_ACK;
                    station.receivingFrames.remove(frame);
                    station.mSender.onPostRecvACK((AckFrame) frame);
                }
            }
        }, frame.getEndDuration());
    }
}
