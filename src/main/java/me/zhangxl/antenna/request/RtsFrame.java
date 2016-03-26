package me.zhangxl.antenna.request;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class RtsFrame extends me.zhangxl.antenna.request.Frame {

    public RtsFrame(int srcId, int targetId, long length) {
        super(srcId, targetId, length);
    }

    @Override
    public long getTransmitDuration() {
        return 0;
    }
}
