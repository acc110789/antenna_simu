package me.zhangxl.antenna.util;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;

/**
 * Created by zhangxiaolong on 16/5/15.
 */
public class Constant {

    /**
     * @param slots 最多允许的slots数量
     * @return Pcp节点发送NextRoundFrame之后等待Rts的超时时间
     */
    public static double getPcpRtsTimeOut(int slots){
        return PrecisionUtil.add(Config.getInstance().getSifs(),
                PrecisionUtil.mul(slots, Config.getInstance().getSlotLength()),
                RtsFrame.getRtsTimeOut());
    }

    /**
     * @param slots
     * @return 正常情况(不是超时)如果由rts发送过来就在这个时候准备处理了
     */
    public static double getPcpRtsDeadLine(int slots){
        return PrecisionUtil.add(Config.getInstance().getSifs(),
                PrecisionUtil.mul(slots, Config.getInstance().getSlotLength()),
                RtsFrame.getFrameTimeLength());
    }

    public static double getDataChannelDeadLine(){
        return PrecisionUtil.add(Config.getInstance().getSifs(),
                DataFrame.getFrameTimeLength(),
                Config.getInstance().getSifs(),
                AckFrame.getFrameTimeLength());
    }
}
