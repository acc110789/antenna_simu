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
    public void doLogic(final Frame frame) {
        assert frame instanceof DataFrame;
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(!frame.isDirty()){
                    assert station.getCurrentStatus() == Status.WAITING_DATA;
                    station.receivingFrames.remove(frame);
                    station.mReceiver.onPostRecvData((DataFrame) frame);
                }
            }
        }, frame.getEndDuration());
    }
}
