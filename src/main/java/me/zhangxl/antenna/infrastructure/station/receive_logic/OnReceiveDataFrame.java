package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceiveDataFrame extends OnReceiveFrameLogic {


    public OnReceiveDataFrame(Station station, Frame frame) {
        super(station, frame);
    }

    @Override
    void onPre() {
        assert frame instanceof DataFrame;
        try {
            assert station.getCurrentStatus() == Status.WAITING_DATA;
        } catch (Throwable throwable){
            System.err.println(station.getCurrentStatus());
            throw throwable;
        }
        station.setCurrentStatus(Status.RECEIVING_DATA);
    }

    @Override
    void onPost() {
        assert station.getCurrentStatus() == Status.RECEIVING_DATA;
        station.getReceiver().onPostRecvData((DataFrame) frame);
    }
}
