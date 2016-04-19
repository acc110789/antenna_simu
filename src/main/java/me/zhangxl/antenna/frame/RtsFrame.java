package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class RtsFrame extends Frame {

    private static final int rtsLength = Config.getInstance().getRtsLength();

    public RtsFrame(int srcId, int targetId) {
        super(srcId, targetId, rtsLength);
    }

    public static double getRtsTimeOut(){
        double oldTimeout = Config.getInstance().getSifs() + Config.getInstance().getDifs()
                + Config.round(rtsLength / Config.getInstance().getBandWidth());
        return oldTimeout - Config.getInstance().getDifs();
    }

    @Override
    public double getNavDuration() {
        return Config.getInstance().getSifs() + Config.round(CtsFrame.ctsLength/Config.getInstance().getBandWidth())
                +Config.getInstance().getSifs() + Config.round(DataFrame.frameLength/Config.getInstance().getBandWidth())
                +Config.getInstance().getSifs() + Config.round(AckFrame.ackLength/Config.getInstance().getBandWidth());
    }
}
