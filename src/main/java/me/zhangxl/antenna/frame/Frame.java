package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;

/**
 * 抽象类,各种Frame
 * Created by zhangxiaolong on 16/3/24.
 */
public abstract class Frame {
    final int srcId;
    private final long length;
    private final int targetId;
    private boolean collision = false;
    private double startTime = -1;
    private boolean scheduled = false;

    Frame(int srcId, int targetId, long length){
        this.srcId = srcId;
        this.targetId = targetId;
        this.length = length;
        if(this.length < 0){
            throw new IllegalArgumentException("length is less than 0");
        }
    }

    public void setScheduled(){
        this.scheduled = true;
    }

    public void unsetScheduled(){
        this.scheduled = false;
    }

    public boolean scheduled(){
        return this.scheduled;
    }

    public void setCollision(){
        this.collision = true;
    }

    public boolean collision(){
        return collision;
    }

    /**
     * 收到与不是自己节点的Frame的时候,要设置NAV
     * NAV完毕之后,立马启动一个DIFS,DIFS之后则
     * 开始backOff
     * @return NAV持续的时间
     */
    public abstract double getNavDuration();

    /**
     * @return 将所有的桢数据从原始节点到目标节点所需要的时间
     */
    public double getTransmitDuration(){
        if(length <= 0){
            throw new IllegalArgumentException("length is negative");
        }
        return length / Config.getInstance().getBandWidth();
    }

    private long getLength(){
        return this.length;
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
        return this.startTime + getTransmitDuration();
    }

    public int getSrcId(){
        return srcId;
    }

    public RtsFrame generateRtsFrame(){
        return new RtsFrame(this.srcId,this.targetId);
    }

    /**
     * @return CTSFrame是回复,src和target应该反过来
     */
    public CtsFrame generateCtsFrame(){
        return new CtsFrame(this.targetId,this.srcId);
    }

    /**
     * @return AckFrame,src和target应该反过来
     */
    public AckFrame generateAckFrame(){
        return new AckFrame(this.targetId,this.srcId);
    }

}
