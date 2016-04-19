package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/28.
 */
public class AckFrame extends Frame {

    static final int ackLength = Config.getInstance().getAckLength();

    AckFrame(int srcId, int targetId) {
        super(srcId, targetId, Config.getInstance().getAckLength());
    }

    public static double getAckTimeOut(){
        double oldTimeout = Config.getInstance().getSifs() + Config.getInstance().getDifs()
                + ackLength / Config.getInstance().getBandWidth();
        return oldTimeout - Config.getInstance().getDifs();
    }

    @Override
    public double getNavDuration() {
        throw new IllegalStateException("ack can not hava nav");
    }
}
