package me.zhangxl.antenna.request;

/**
 * 抽象类,各种Request
 * Created by zhangxiaolong on 16/3/24.
 */
public abstract class Frame {
    private final long length;

    private final int srcId;

    private final int targetId;

    public Frame(int srcId, int targetId, long length){
        this.srcId = srcId;
        this.targetId = targetId;
        this.length = length;
    }

    abstract public long getTransmitDuration();
}
