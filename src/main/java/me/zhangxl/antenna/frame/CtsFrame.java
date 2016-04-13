package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class CtsFrame extends Frame {

    static final int ctsLength = Config.getInstance().getCtsLength();

    CtsFrame(int srcId, int targetId) {
        super(srcId, targetId, ctsLength);
    }

    public static double getCtsTimeOut(){
        return Config.getInstance().getSifs() + Config.getInstance().getDifs()
                + ctsLength / Config.getInstance().getBandWidth();
    }

    @Override
    public double getNavDuration() {
        return Config.getInstance().getSifs() + DataFrame.frameLength/Config.getInstance().getBandWidth()
                +Config.getInstance().getSifs() + AckFrame.ackLength/Config.getInstance().getBandWidth();
    }
}
