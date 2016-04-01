package me.zhangxl.antenna.request;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;

import java.util.Random;

/**
 * 代表一个数据桢,只需要关心数据桢的长度,因为数据桢的长度
 * 决定传输需要消耗的时间.
 * Created by zhangxiaolong on 16/3/24.
 */
public class DataFrame extends Frame {

    private static Logger logger = new Logger(DataFrame.class);

    private static Random random = new Random(System.currentTimeMillis());
    private static int serialNum = 0;
    private long startTime = Config.DEFAULT_DATA_FRAME_START_TIME;
    private int collisionTimes = Config.DEFAULT_DATA_FRAME_COLLISION_TIME;
    private int backOff;
    private int id;
    /**
     * 表明当前正在发生碰撞
     */
    private boolean collision = false;

    public DataFrame(int srcId, int targetId, long length) {
        this(srcId,targetId,length,nextSerialNum());
    }

    public DataFrame(int srcId, int targetId, long length,int id) {
        super(srcId, targetId, length);
        this.id = id;
    }

    private static int nextSerialNum(){
        return ++serialNum;
    }

    public int getSerialNum(){
        return id;
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
        int contentionWindow = Math.min(Config.getInstance().getDefaultCW() + this.collisionTimes,Config.getInstance().getMaxCW());
        int window = (int) Math.pow(2, contentionWindow);
        backOff = random.nextInt(window);
        if(Logger.DEBUG_FRAME){
            logger.log("station :%d  new DataFrame callBack window:%d   frame id:%d",srcId,backOff,getSerialNum());
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
