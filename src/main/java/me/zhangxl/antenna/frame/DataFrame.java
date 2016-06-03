package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.SimuLoggerManager;
import me.zhangxl.antenna.util.TimeLogger;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/**
 * 代表一个数据桢,只需要关心数据桢的长度,因为数据桢的长度
 * 决定传输需要消耗的时间.
 * Created by zhangxiaolong on 16/3/24.
 */
public class DataFrame extends Frame {

    private static Logger logger = SimuLoggerManager.getLogger(DataFrame.class.getSimpleName());
    //整个桢的长度,包括物理层的header,mac层的header,包括数据的长度
    private static final long frameLength = Config.getInstance().getFixDataLength()
            + Config.getInstance().getPhyHeader()
            + Config.getInstance().getMacHeader();
    private static Random random = new Random(System.currentTimeMillis());
    private static int serialNum = 0;
    //仅仅数据部分的长度
    private static long dataLength = Config.getInstance().getFixDataLength();
    private double startTime = Config.DEFAULT_DATA_FRAME_START_TIME;
    private int failTimes = Config.DEFAULT_DATA_FRAME_COLLISION_TIME;
    private int backOff;
    private int id;

    public DataFrame(int srcId, int targetId) {
        this(srcId, targetId, nextSerialNum());
    }

    private DataFrame(int srcId, int targetId, int id) {
        super(srcId, targetId, frameLength);
        this.id = id;
    }

    private static int nextSerialNum() {
        return ++serialNum;
    }

    public void addCollitionTimes() {
        failTimes++;
        if (TimeLogger.DEBUG_FRAME) {
            logger.info("Station %d send data frame fail,update backoff", srcId);
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
        if (this.failTimes == Config.DEFAULT_DATA_FRAME_COLLISION_TIME) {
            this.failTimes = 0;
        }
        updateBackOff();
    }

    private void updateBackOff() {
        int contentionWindow = Math.min(Config.getInstance().getDefaultCW() + this.failTimes, Config.getInstance().getMaxCW());
        int window = (int) Math.pow(2, contentionWindow);
        backOff = random.nextInt(window);
        if (TimeLogger.DEBUG_FRAME) {
            logger.info("source station :%d  destination :%d  new window:%d ", srcId, getTargetId(), backOff);
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

    public int getBackOff() {
        return backOff;
    }

    private void checkBackOff() {
        if (backOff < 0) {
            throw new IllegalStateException("backOff is less than 0");
        }
    }

    public long getLength() {
        return dataLength;
    }
}
