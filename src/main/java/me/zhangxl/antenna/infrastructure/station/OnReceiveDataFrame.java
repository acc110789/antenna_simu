package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceiveDataFrame extends OnReceiveFrameLogic {

    public OnReceiveDataFrame(Station station) {
        super(station);
    }

    @Override
    void doLogic(final Frame frame) {
        assert frame instanceof DataFrame;
        assert station.getCurrentStatus() == Status.WAITING_DATA_FRAME;
        station.setCurrentStatus(Status.RECEIVING_DATA);

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                assert station.getCurrentStatus() == Status.RECEIVING_DATA;
                station.receivingFrames.remove(frame);
                station.mReceiver.onPostRecvData((DataFrame) frame);
            }
        }, frame.getEndDuration());
    }
}