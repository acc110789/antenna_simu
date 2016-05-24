package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class RtsFrame extends Frame implements Navable {

    private static final int rtsLength = Config.getInstance().getRtsLength();
    private static final double frameTimeLength;

    private static double rtsTimeOut;

    static {
        frameTimeLength = PrecisionUtil.div(rtsLength,Config.getInstance().getBandWidth());
        rtsTimeOut = PrecisionUtil.add(Config.getInstance().getSifs(),
                Config.getInstance().getDifs(),
                frameTimeLength);

        rtsTimeOut = PrecisionUtil.sub(rtsTimeOut ,Config.getInstance().getDifs());
    }

    public RtsFrame(int srcId, int targetId) {
        super(srcId, targetId, rtsLength);
    }

    public static double getRtsTimeOut(){
        return rtsTimeOut;
    }


    private static final double navDuration =
            PrecisionUtil.add(
                    Config.getInstance().getSifs(),
                    PrecisionUtil.mul(Config.getInstance().getPart(),
                            PrecisionUtil.div(PtsFrame.frameLength,Config.getInstance().getBandWidth())),
                    PtsFrame.baseNav);

    @Override
    public double getNavDuration() {
        return navDuration;
    }

    public static double getRtsNavDuration(){
        return navDuration;
    }

    public static double getFrameTimeLength(){
        return frameTimeLength;
    }

}
