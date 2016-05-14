package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;

import static me.zhangxl.antenna.infrastructure.station.BaseRole.Status;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
class OnReceiveNextRoundFrame extends OnReceiveFrameLogic {

    OnReceiveNextRoundFrame(Station station) {
        super(station);
    }

    @Override
    void doLogic(final Frame frame) {
        assert frame instanceof NextRoundFrame;
        assert station.getCurrentStatus() == Status.WAITING_NEXT_ROUND;
        station.setCurrentStatus(BaseRole.Status.RECEIVING_NEXT_ROUND_FRAME);

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                assert station.getCurrentStatus() == Status.RECEIVING_NEXT_ROUND_FRAME;
                station.receivingFrames.remove(frame);
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        station.onNextRound(((NextRoundFrame) frame).getSlots());
                    }
                }, Config.getInstance().getSifs());
            }
        }, frame.getEndDuration());
    }
}
