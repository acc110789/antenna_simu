package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceiveRtsFrame extends OnReceiveFrameLogic {

    public OnReceiveRtsFrame(Station station, Frame frame) {
        super(station, frame);
    }

    @Override
    public void onPreFrame() {
        assert frame instanceof RtsFrame;
    }

    @Override
    public void onClearFrame() {
        //station 收到rts没有什么用处,等时间到了就不予理睬了
        // TODO: 16/5/22 要不要assert
        station.mReceiver.onPostRecvRTS((RtsFrame) frame);
        // TODO: 16/5/21 要不要再等一段时间然后让station回复到idle的状态
    }
}
