package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.Frame;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public abstract class OnReceiveFrameLogic {
    protected final Station station;
    public OnReceiveFrameLogic(Station station){
        this.station = station;
    }

    protected abstract void doLogic(Frame frame);
}
