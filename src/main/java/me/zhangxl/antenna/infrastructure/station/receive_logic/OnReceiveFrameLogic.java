package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public abstract class OnReceiveFrameLogic {
    protected final Station station;
    protected final Frame frame;
    public OnReceiveFrameLogic(Station station,Frame frame){
        this.station = station;
        this.frame = frame;
    }

    public void doLogic(){
        onPre();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                station.getReceivingFrames().remove(frame);
                onPost();
            }
        },frame.getEndDuration(), TimeTask.RECEIVE);
    }

    abstract void onPre();
    abstract void onPost();
}
