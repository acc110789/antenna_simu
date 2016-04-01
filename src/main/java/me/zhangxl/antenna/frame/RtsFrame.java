package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class RtsFrame extends me.zhangxl.antenna.frame.Frame {

    RtsFrame(int srcId, int targetId) {
        super(srcId, targetId, Config.getInstance().getRtsLength());
    }

}
