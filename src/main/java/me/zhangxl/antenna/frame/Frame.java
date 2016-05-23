package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * 抽象类,各种Frame
 * Created by zhangxiaolong on 16/3/24.
 */
public abstract class Frame implements Cloneable{
    final int srcId;
    private final long length;
    private final int targetId;
    private boolean dirty = false;
    private double startTime = -1;
    private int sendTargetId = -1;

    Frame(int srcId, int targetId, long length){
        this.srcId = srcId;
        this.targetId = targetId;
        this.length = length;
        if(this.length < 0){
            throw new IllegalArgumentException("length is less than 0");
        }
    }

    public void setDirty(){
        this.dirty = true;
    }

    public boolean isDirty(){
        return dirty;
    }

    public double getEndDuration(){
        return PrecisionUtil.sub(getEndTime(),TimeController.getInstance().getCurrentTime());
    }

    /**
     * @return 将所有的桢数据从原始节点到目标节点所需要的时间
     */
    public double getTransmitDuration(){
        if(length <= 0){
            throw new IllegalArgumentException("length is negative");
        }
        return PrecisionUtil.div(length,Config.getInstance().getBandWidth());
    }

    /**
     * @return 该frame的发送目标,即mac地址中的第二个地址,发送目标
     */
    public int getTargetId(){
        return targetId;
    }

    /**
     * @return 定向天线返回发送扇区中的某一个目标的id,用于反推出给frame是具体向哪一个扇区发送的
     * 一般情况下与 {@link #getTargetId()}
     */
    public int getSendTargetId(){
        return getTargetId();
    }

    public void setStartTimeNow(){
        this.startTime = TimeController.getInstance().getCurrentTime();
    }

    public double getStartTime(){
        return this.startTime;
    }

    public double getEndTime(){
        if(this.startTime < 0){
            throw new IllegalArgumentException("startTime is less than 0");
        }
        return PrecisionUtil.add(this.startTime,getTransmitDuration());
    }

    public int getSrcId(){
        return srcId;
    }

    public RtsFrame generateRtsFrame(){
        return new RtsFrame(this.srcId,this.targetId);
    }

    /**
     * @return AckFrame,src和target应该反过来
     */
    public AckFrame generateAckFrame(){
        return new AckFrame(this.targetId,this.srcId);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
