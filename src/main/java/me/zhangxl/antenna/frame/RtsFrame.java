package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class RtsFrame extends Frame {

    private static final int rtsLength = Config.getInstance().getRtsLength();

    public RtsFrame(int srcId, int targetId, int fre) {
        super(srcId, targetId, rtsLength, fre);
    }
}
