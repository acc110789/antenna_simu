package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.infrastructure.base.ChannelManager;
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
    private Integer fre; //表明这个frame的传输频率是多少
    private boolean complete = true;

    Frame(int srcId, int targetId, long length, int fre){
        this.srcId = srcId;
        this.targetId = targetId;
        this.length = length;
        this.fre = fre;
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

    public int getTargetId(){
        return targetId;
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

    public int getFre(){
        return this.fre;
    }

    public void setFre(int fre){
        this.fre = fre;
    }

    public RtsFrame generateRtsFrame(){
        return new RtsFrame(this.srcId,this.targetId, ChannelManager.getInstance().getRandomRtsChannel());
    }

    /**
     * @return AckFrame,src和target应该反过来
     */
    public AckFrame generateAckFrame(){
        return new AckFrame(this.targetId,this.srcId, this.fre);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
