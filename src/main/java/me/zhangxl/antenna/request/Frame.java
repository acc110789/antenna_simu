package me.zhangxl.antenna.request;

import me.zhangxl.antenna.util.Config;

/**
 * 抽象类,各种Frame
 * Created by zhangxiaolong on 16/3/24.
 */
public abstract class Frame {
    private final long length;

    protected final int srcId;

    private final int targetId;

    Frame(int srcId, int targetId, long length){
        this.srcId = srcId;
        this.targetId = targetId;
        this.length = length;
    }

    /**
     * @return 将所有的桢数据从原始节点到目标节点所需要的时间
     */
    public float getTransmitDuration(){
        if(length <= 0){
            throw new IllegalArgumentException("length is negative");
        }
        return length / Config.BAND_WIDTH;
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
}
