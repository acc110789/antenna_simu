package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/28.
 */
public class AckFrame extends Frame {

    private static final int ackLength = Config.getAckLength();

    AckFrame(int srcId, int targetId, int fre) {
        super(srcId, targetId, ackLength, fre);
    }
}
