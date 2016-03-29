package me.zhangxl.antenna.request;

/**
 * Created by zhangxiaolong on 16/3/28.
 */
public class AckFrame extends Frame {

    public AckFrame(int srcId, int targetId, long length) {
        super(srcId, targetId, length);
    }

    @Override
    public long getTransmitDuration() {
        return 0;
    }
}
