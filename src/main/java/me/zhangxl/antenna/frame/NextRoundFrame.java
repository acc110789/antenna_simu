package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * 这个节点由PCP节点全向发出(在2.4GHz或者5GHz任意选出一个频率)
 * 表明NextRound的竞争开始,桢内容仅仅包含两个字节的内容,内容
 * 具体来说是一个数据,记为num,表明让周围所有准备竞争的节点最多退避
 * num个slot,如果在num个slot期间slot已经退避到0,则允许这个竞争
 * 节点随机选择一个频率发送RTS给PCP节点,如果在num个slot还没有退避
 * 到0,则等待PCP节点下一个发出的NextRoundFrame继续退避.
 * Created by zhangxiaolong on 16/5/12.
 */
public class NextRoundFrame extends Frame {

    private static final long frameLength = Config.getInstance().getPhyHeader() +
            Config.getInstance().getMacHeader() +
            2 * 8;//2个字节表明下一轮最有竞争节点可能的退避窗口
    private int slots = -1;

    private static final double frameTimeLength = PrecisionUtil.div(frameLength,
            Config.getInstance().getBandWidth());

    public NextRoundFrame(int srcId, int targetId, int fre, int slots) {
        super(srcId, targetId, frameLength, fre);
        this.slots = slots;
    }

    public static double getFrameTimeLength(){
        return frameTimeLength;
    }

    @Override
    public double getNavDuration() {
        return 0;
    }

    public int getSlots(){
        return this.slots;
    }
}
