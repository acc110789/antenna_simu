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
        return Config.getInstance().getSifs() + Config.getInstance().getDifs()
                + rtsLength / Config.getInstance().getBandWidth();
    }

    @Override
    public double getNavDuration() {
        return Config.getInstance().getSifs() + CtsFrame.ctsLength/Config.getInstance().getBandWidth()
                +Config.getInstance().getSifs() + DataFrame.frameLength/Config.getInstance().getBandWidth()
                +Config.getInstance().getSifs() + AckFrame.ackLength/Config.getInstance().getBandWidth();
    }
}
