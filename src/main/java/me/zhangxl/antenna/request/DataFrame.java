package me.zhangxl.antenna.request;

import me.zhangxl.antenna.infrastructure.InfraConstant;

import java.util.Random;

/**
 * 代表一个数据桢,只需要关心数据桢的长度,因为数据桢的长度
 * 决定传输需要消耗的时间
 * Created by zhangxiaolong on 16/3/24.
 */
public class DataFrame extends Frame {

    private long startTime = InfraConstant.DEFAULT_DATA_FRAME_START_TIME;

    private int collisionTimes = InfraConstant.DEFAULT_DATA_FRAME_COLLISION_TIME;

    private int backOff;

    public DataFrame(int srcId, int targetId, long length){
        super(srcId,targetId,length);
    }

    public void addCollitionTimes(){
        collisionTimes ++;
    }

    /**
     * 初始化Frame
     */
    public void init(){
        if(this.startTime == InfraConstant.DEFAULT_DATA_FRAME_START_TIME) {
            this.startTime = System.currentTimeMillis();
        }
        if(this.collisionTimes == InfraConstant.DEFAULT_DATA_FRAME_COLLISION_TIME){
            this.collisionTimes = 0;
        }
        int window = (int) Math.pow(2,4+this.collisionTimes);
        Random random = new Random(System.currentTimeMillis());
        backOff = random.nextInt(window);
    }

    public void countDownBackOff(){
        backOff--;
        checkBackOff();
    }

    public boolean canBeSent(){
        checkBackOff();
        return backOff == 0;
    }

    private void checkBackOff(){
        if(backOff < 0){
            throw new IllegalStateException("backOff is less than 0");
        }
    }

}
