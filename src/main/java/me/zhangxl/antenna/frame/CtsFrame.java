package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * 新的Frame应该是PCP节点全向发送给周围的所有节点,这个Frame
 * Created by zhangxiaolong on 16/3/24.
 */
public class CtsFrame extends Frame {

    static final int ctsLength = Config.getInstance().getCtsLength();
    private static final double frameTimeLength;

    CtsFrame(int srcId, int targetId , int fre) {
        super(srcId, targetId, ctsLength, fre);
    }

    private static double ctsTimeOut ;

    static {
        frameTimeLength = PrecisionUtil.div(ctsLength,Config.getInstance().getBandWidth());
        ctsTimeOut = PrecisionUtil.add(Config.getInstance().getSifs(),
                Config.getInstance().getDifs(),
                frameTimeLength);

        ctsTimeOut = PrecisionUtil.sub(ctsTimeOut,Config.getInstance().getDifs());
    }

    public static double getCtsTimeOut(){
        return ctsTimeOut;
    }

    private static double navDuration = PrecisionUtil.add(Config.getInstance().getSifs(),
            PrecisionUtil.div(DataFrame.frameLength,Config.getInstance().getBandWidth()),
            Config.getInstance().getSifs(),
            PrecisionUtil.div(AckFrame.ackLength,Config.getInstance().getBandWidth()));

    @Override
    public double getNavDuration() {
        return navDuration;
    }

    public static double getFrameTimeLength(){
        return frameTimeLength;
    }
}
