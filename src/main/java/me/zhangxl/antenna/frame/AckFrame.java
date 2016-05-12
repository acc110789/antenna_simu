package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * Created by zhangxiaolong on 16/3/28.
 */
public class AckFrame extends Frame {

    static final int ackLength = Config.getInstance().getAckLength();

    AckFrame(int srcId, int targetId, int fre) {
        super(srcId, targetId, Config.getInstance().getAckLength(),fre);
    }

    private static double ackTimeOut ;

    static {
        ackTimeOut = PrecisionUtil.add(Config.getInstance().getSifs(),
                Config.getInstance().getDifs(),
                PrecisionUtil.div(ackLength,Config.getInstance().getBandWidth()));

        ackTimeOut = PrecisionUtil.sub(ackTimeOut,Config.getInstance().getDifs());
    }

    public static double getAckTimeOut(){
        return ackTimeOut;
    }

    @Override
    public double getNavDuration() {
        throw new IllegalStateException("ack can not hava nav");
    }
}
