package me.zhangxl.antenna.request;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class CtsFrame extends Frame {

    public CtsFrame(int srcId, int targetId) {
        super(srcId, targetId, Config.CTS_LENGTH);
    }

    @Override
    public long getTransmitDuration() {
        return 0;
    }
}
