package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象类,各种Frame
 * Created by zhangxiaolong on 16/3/24.
 */
public abstract class Frame {
    final int srcId;
    private final long length;
    private final int targetId;

    Frame(int srcId, int targetId, long length){
        this.srcId = srcId;
        this.targetId = targetId;
        this.length = length;
        if(this.length < 0){
            throw new IllegalArgumentException("length is less than 0");
        }
    }

    /**
     * @return 将所有的桢数据从原始节点到目标节点所需要的时间
     */
    public float getTransmitDuration(){
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

    public static GarbageFrame generateGarbageFrame(List<Frame> frames){
        if(frames.size() <= 1){
            throw new IllegalArgumentException("GarbageFrame should be generated from at least 2 frames");
        }
        List<Integer> srcIds = new ArrayList<>();
        List<Integer> targetIds = new ArrayList<>();
        long maxLength = -1;
        for(Frame frame : frames){
            srcIds.add(frame.getSrcId());
            targetIds.add(frame.getTargetId());
            maxLength = Math.max(maxLength,frame.getLength());
        }
        return new GarbageFrame(srcIds,targetIds,maxLength);
    }
}
