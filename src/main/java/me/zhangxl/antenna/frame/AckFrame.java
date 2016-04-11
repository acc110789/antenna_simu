package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/28.
 */
public class AckFrame extends Frame {

    private static final int ackLength = Config.getInstance().getAckLength();

    AckFrame(int srcId, int targetId) {
        super(srcId, targetId, Config.getInstance().getAckLength());
    }

    public static float getAckTimeOut(){
        return Config.getInstance().getSifs() + Config.getInstance().getDifs()
                + ackLength / Config.getInstance().getBandWidth();
    }

}
