package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.station.BaseRole;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceiveDataFrame extends OnReceiveFrameLogic {


    public OnReceiveDataFrame(Station station, Frame frame) {
        super(station, frame);
    }

    @Override
    void onPreFrame() {
        assert frame instanceof DataFrame;
    }

    @Override
    public void onClearFrame() {
        assert station.getCurrentStatus() == BaseRole.Status.WAITING_DATA;
        station.receivingFrames.remove(frame);
        station.mReceiver.onPostRecvData((DataFrame) frame);
    }
}
