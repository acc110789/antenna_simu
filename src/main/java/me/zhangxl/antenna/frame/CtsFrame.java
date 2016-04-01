package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class CtsFrame extends Frame {

    CtsFrame(int srcId, int targetId) {
        super(srcId, targetId, Config.getInstance().getCtsLength());
    }

}
