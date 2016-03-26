package me.zhangxl.antenna.request;

/**
 * 代表一个数据桢,只需要关心数据桢的长度,因为数据桢的长度
 * 决定传输需要消耗的时间
 * Created by zhangxiaolong on 16/3/24.
 */
public class DataFrame extends Frame {

    private long startTime;

    private int collisionTimes = 0;

    public DataFrame(int srcId, int targetId, long length){
        super(srcId,targetId,length);
    }

    @Override
    public long getTransmitDuration(){
        // TODO: 16/3/24  根据数据桢的长度计算出传输需要消耗的时间
        return 100;
    }

    public void setStartTimeNow(){
        this.startTime = System.currentTimeMillis();
    }

    public void addCollitionTimes(){
        collisionTimes ++;
    }

}
