package me.zhangxl.antenna.request;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;

import java.util.Random;

/**
 * 代表一个数据桢,只需要关心数据桢的长度,因为数据桢的长度
 * 决定传输需要消耗的时间
 * Created by zhangxiaolong on 16/3/24.
 */
public class DataFrame extends Frame {

    private static Logger logger = new Logger(DataFrame.class);

    private static Random random = new Random(System.currentTimeMillis());

    private long startTime = Config.DEFAULT_DATA_FRAME_START_TIME;

    private int collisionTimes = Config.DEFAULT_DATA_FRAME_COLLISION_TIME;

    private int backOff;

    /**
     * 表明当前正在发生碰撞
     */
    private boolean collision = false;

    public DataFrame(int srcId, int targetId, long length) {
        super(srcId, targetId, length);
    }

    public void addCollitionTimes() {
        collisionTimes++;
        collision = true;
    }

    public boolean isCollision() {
        return collision;
    }

    public void unsetCollision() {
        collision = false;
        if(Logger.DEBUG_COLLISION){
            logger.log("resolve collision data frame srcid:%d",srcId);
        }
        updateBackOff();
    }

    /**
     * 初始化Frame
     */
    public void init() {
        if (this.startTime == Config.DEFAULT_DATA_FRAME_START_TIME) {
            this.startTime = System.currentTimeMillis();
        }
        if (this.collisionTimes == Config.DEFAULT_DATA_FRAME_COLLISION_TIME) {
            this.collisionTimes = 0;
        }
        updateBackOff();
    }

    private void updateBackOff() {
        int window = (int) Math.pow(2, 1 + this.collisionTimes);
        backOff = random.nextInt(window);
        if(Logger.DEBUG_FRAME){
            logger.log("station :%d  new DataFrame callBack window:%d",srcId,backOff);
        }
    }

    public void countDownBackOff() {
        backOff--;
        checkBackOff();
    }

    public boolean canBeSent() {
        checkBackOff();
        return backOff == 0;
    }

    private void checkBackOff() {
        if (backOff < 0) {
            throw new IllegalStateException("backOff is less than 0");
        }
    }

}
