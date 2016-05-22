package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * Created by zhangxiaolong on 16/3/28.
 */
public class AckFrame extends Frame {

    static final int ackLength = Config.getInstance().getAckLength();

    AckFrame(int srcId, int targetId) {
        super(srcId, targetId, Config.getInstance().getAckLength());
    }

    private static double ackTimeOut ;
    private static double frameTimeLength;

    static {
        frameTimeLength = PrecisionUtil.div(ackLength,Config.getInstance().getBandWidth());
        ackTimeOut = PrecisionUtil.add(Config.getInstance().getSifs(),
                Config.getInstance().getDifs(), frameTimeLength);
        ackTimeOut = PrecisionUtil.sub(ackTimeOut,Config.getInstance().getDifs());
    }

    public static double getFrameTimeLength(){
        return frameTimeLength;
    }

    public static double getAckTimeOut(){
        return ackTimeOut;
    }
}
