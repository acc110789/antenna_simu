package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class CtsFrame extends Frame {

    private static final int ctsLength = Config.getInstance().getCtsLength();

    CtsFrame(int srcId, int targetId) {
        super(srcId, targetId, ctsLength);
    }
}
