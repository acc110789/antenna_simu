package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.station.nav.DataNav;

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
        if(frame.getTargetId() == station.getId()) {
            //确实是发送给本station的。
            station.mReceiver.onPostRecvData((DataFrame) frame);
        } else {
            new DataNav(station).startNav();
        }
    }
}
