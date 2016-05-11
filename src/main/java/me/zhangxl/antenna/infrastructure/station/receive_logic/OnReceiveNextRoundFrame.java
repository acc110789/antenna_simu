package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.NextRoundFrame;
import me.zhangxl.antenna.infrastructure.base.Stateful;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.Constant;

import static me.zhangxl.antenna.infrastructure.base.Stateful.Status;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceiveNextRoundFrame extends OnReceiveFrameLogic {


    public OnReceiveNextRoundFrame(Station station, Frame frame) {
        super(station, frame);
    }

    @Override
    void onPre() {
        assert frame instanceof NextRoundFrame;
        assert station.getCurrentStatus() == Status.WAITING_NEXT_ROUND;
        station.setCurrentStatus(Stateful.Status.RECEIVING_NEXT_ROUND_FRAME);
    }

    @Override
    void onPost() {
        assert station.getCurrentStatus() == Status.RECEIVING_NEXT_ROUND_FRAME;
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                assert station.getCurrentStatus() == Status.RECEIVING_NEXT_ROUND_FRAME;
                station.onNextRound(((NextRoundFrame) frame).getSlots());
            }
        }, Constant.getNormalCoolingDuration());
    }
}
