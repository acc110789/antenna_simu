package me.zhangxl.antenna.request;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class RtsFrame extends me.zhangxl.antenna.request.Frame {

    RtsFrame(int srcId, int targetId) {
        super(srcId, targetId, Config.RTS_LENGTH);
    }

}
