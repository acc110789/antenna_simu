package me.zhangxl.antenna.request;

/**
 * 抽象类,各种Request
 * Created by zhangxiaolong on 16/3/24.
 */
public abstract class Frame {
    protected final long length;

    protected final int srcId;

    protected final int targetId;

    public Frame(int srcId, int targetId, long length){
        this.srcId = srcId;
        this.targetId = targetId;
        this.length = length;
    }

    abstract public long getTransmitDuration();

    public RtsFrame generateRtsFrame(){
        return new RtsFrame(this.srcId,this.targetId);
    }

    public CtsFrame generateCtsFrame(){
        return new CtsFrame(this.srcId,this.targetId);
    }

    public AckFrame generateAckFrame(){
        return new AckFrame(this.srcId,this.targetId);
    }
}
