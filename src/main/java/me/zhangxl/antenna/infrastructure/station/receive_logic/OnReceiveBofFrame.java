package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.BofFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Constant;

import static me.zhangxl.antenna.infrastructure.base.Stateful.Status;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceiveBofFrame extends OnReceiveFrameLogic {

    public OnReceiveBofFrame(Station station, Frame frame) {
        super(station, frame);
    }

    @Override
    void onPre() {
        assert frame instanceof BofFrame;
        assert station.getCurrentStatus() == Status.WAITING_BACK_OFF;
        station.setCurrentStatus(Status.RECEIVING_NEXT_ROUND_FRAME);
    }

    @Override
    void onPost() {
        assert station.getCurrentStatus() == Status.RECEIVING_NEXT_ROUND_FRAME;
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                assert station.getCurrentStatus() == Status.RECEIVING_NEXT_ROUND_FRAME;
                station.onNextRound((BofFrame) frame);
            }
        }, Constant.getNormalCoolingDuration());//收到BofFrame之后仍然需要等待difs之后
    }
}
