package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.station.nav.RtsNav;

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
        // 比如在wait DataFrame期间,对方没有发送DataFrame,但是另外一个节点发送了Rts,这种情况下,
        //要不要设置nav。
        // 在这种情况下,暂时先nav吧
        if(frame.getTargetId() == station.getId()) {
            station.mReceiver.onPostRecvRTS((RtsFrame) frame);
        } else {
            //RTS不是发给自己的,则设置NAV
            //不是发给本Station的,这种情况下应当设置NAV向量
            new RtsNav(station).startNav();
        }
    }
}
