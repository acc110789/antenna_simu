package me.zhangxl.antenna.request;

import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/3/28.
 */
public class AckFrame extends Frame {

    AckFrame(int srcId, int targetId) {
        super(srcId, targetId, Config.getInstance().getAckLength());
    }

}
