package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class RtsFrame extends Frame {

    private static final int rtsLength = Config.getInstance().getRtsLength();
    private static final double rtsTimeLength;

    private static double rtsTimeOut;

    static {
        rtsTimeLength = PrecisionUtil.div(rtsLength,Config.getInstance().getBandWidth());
        rtsTimeOut = PrecisionUtil.add(Config.getInstance().getSifs(),
                Config.getInstance().getDifs(),
                rtsTimeLength);

        rtsTimeOut = PrecisionUtil.sub(rtsTimeOut ,Config.getInstance().getDifs());
    }

    public RtsFrame(int srcId, int targetId) {
        super(srcId, targetId, rtsLength);
    }

    public static double getRtsTimeOut(){
        return rtsTimeOut;
    }

    private static final double navDuration = PrecisionUtil.add(Config.getInstance().getSifs(),
            PrecisionUtil.div(CtsFrame.ctsLength,Config.getInstance().getBandWidth()),
            Config.getInstance().getSifs(),
            PrecisionUtil.div(DataFrame.frameLength,Config.getInstance().getBandWidth()),
            Config.getInstance().getSifs(),
            PrecisionUtil.div(AckFrame.ackLength,Config.getInstance().getBandWidth()));

    @Override
    public double getNavDuration() {
        return navDuration;
    }

    public static double getTimeLength(){
        return rtsTimeLength;
    }
}
