package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class RtsFrame extends Frame {

    private static final int rtsLength = Config.getInstance().getRtsLength();

    RtsFrame(int srcId, int targetId) {
        super(srcId, targetId, rtsLength);
    }

    public static float getRtsTimeOut(){
        return Config.getInstance().getSifs() + Config.getInstance().getDifs()
                + rtsLength / Config.getInstance().getBandWidth();
    }

}
