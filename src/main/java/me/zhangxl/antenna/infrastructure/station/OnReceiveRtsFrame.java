package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
class OnReceiveRtsFrame extends OnReceiveFrameLogic {

    OnReceiveRtsFrame(Station station) {
        super(station);
    }

    @Override
    public void doLogic(final Frame frame) {
        //station 收到rts没有什么用处,等时间到了就不予理睬了
        assert frame instanceof RtsFrame;
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                station.receivingFrames.remove(frame);
                if(!frame.isDirty()){
                    // TODO: 16/5/22 要不要assert
                    station.mReceiver.onPostRecvRTS((RtsFrame) frame);
                }
            }
        }, frame.getEndDuration());
        // TODO: 16/5/21 要不要再等一段时间然后让station回复到idle的状态
    }
}
