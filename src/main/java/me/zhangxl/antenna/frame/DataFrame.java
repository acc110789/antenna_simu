package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;
import me.zhangxl.antenna.util.PrecisionUtil;

import java.util.Random;

/**
 * 代表一个数据桢,只需要关心数据桢的长度,因为数据桢的长度
 * 决定传输需要消耗的时间.
 * Created by zhangxiaolong on 16/3/24.
 */
public class DataFrame extends Frame {

    static long frameLength = Config.getInstance().getFixDataLength()
            + Config.getInstance().getPhyHeader()
            + Config.getInstance().getMacHeader();
    private static Logger logger = new Logger(DataFrame.class);
    private static Random random = new Random(System.currentTimeMillis());
    private static int serialNum = 0;
    //数据部分的长度
    private static long dataLength = Config.getInstance().getFixDataLength();
    private double startTime = Config.DEFAULT_DATA_FRAME_START_TIME;
    private int collisionTimes = Config.DEFAULT_DATA_FRAME_COLLISION_TIME;
    private int backOff;
    private int id;
    /**
     * 表明当前正在发生碰撞
     */
    private boolean collision = false;

    public DataFrame(int srcId, int targetId) {
        this(srcId, targetId, nextSerialNum());
    }

    public DataFrame(int srcId, int targetId, int id) {
        super(srcId, targetId, frameLength);
        this.id = id;
    }

    private static double dataTimeOut ;
    static {
        dataTimeOut = PrecisionUtil.add(Config.getInstance().getSifs(),
                Config.getInstance().getDifs(),
                PrecisionUtil.div(frameLength,Config.getInstance().getBandWidth()));
        dataTimeOut = PrecisionUtil.sub(dataTimeOut,Config.getInstance().getDifs());
    }

    public static double getDataTimeOut() {
        return dataTimeOut;
    }

    private static int nextSerialNum() {
        return ++serialNum;
    }

    public int getSerialNum() {
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
        if (Logger.DEBUG_COLLISION) {
            logger.log("Station %d resolve collision data frame", srcId);
        }
        updateBackOff();
    }

    /**
     * 初始化Frame
     */
    public void init() {
        if (this.startTime == Config.DEFAULT_DATA_FRAME_START_TIME) {
            this.startTime = TimeController.getInstance().getCurrentTime();
        }
        if (this.collisionTimes == Config.DEFAULT_DATA_FRAME_COLLISION_TIME) {
            this.collisionTimes = 0;
        }
        updateBackOff();
    }

    private void updateBackOff() {
        int contentionWindow = Math.min(Config.getInstance().getDefaultCW() + this.collisionTimes, Config.getInstance().getMaxCW());
        int window = (int) Math.pow(2, contentionWindow);
        backOff = random.nextInt(window);
        //for Test
        //backOff = 1;
        if (Logger.DEBUG_FRAME) {
            logger.log("station :%d  destination :%d  new window:%d ", srcId, getTargetId(), backOff);
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

    @Override
    public double getNavDuration() {
        throw new IllegalStateException("data frame can not hava nav");
    }

    public long getLength() {
        return dataLength;
    }

}
