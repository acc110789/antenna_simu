package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.station.cool.EifsCooler;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public abstract class OnReceiveFrameLogic {
    protected final Station station;
    protected final Frame frame;

    OnReceiveFrameLogic(Station station, Frame frame) {
        this.station = station;
        this.frame = frame;
    }

    public void doLogic() {
        onPreFrame();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                station.getReceivingFrames().remove(frame);
                if (!frame.isDirty()) {
                    onClearFrame();
                } else if (!hasReceivingFrames()) {
                    //是最后一个脏的frame,则马上等待信道冷却
                    new EifsCooler(station).cool();
                }
            }
        }, frame.getEndDuration(), TimeTask.RECEIVE);
    }

    abstract void onPreFrame();

    abstract void onClearFrame();

    private boolean hasReceivingFrames() {
        return !station.getReceivingFrames().isEmpty();
    }

}
