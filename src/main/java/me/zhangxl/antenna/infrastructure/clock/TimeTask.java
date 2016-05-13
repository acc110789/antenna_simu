package me.zhangxl.antenna.infrastructure.clock;

import me.zhangxl.antenna.util.PrecisionUtil;

/**
 *  {@link TimeController} 持有的ClockTask
 * Created by zhangxiaolong on 16/3/30.
 */
public class TimeTask implements Comparable<TimeTask> {

    private double mTimeToRun;
    private final Runnable mToRun;
    private final long bornTime;
    private static long serialNum = 1;
    private final int mPriority;

    /**
     * 一个原则是在同一个时刻的发送和接受,要保证发送的执行比接受的执行更快
     * 优先级的数值越小,代表优先级越高,则越先被执行
     */
    public static int COMMON_PRIORITY = 0;
    public static int POST_SEND_CTS = COMMON_PRIORITY;
    public static int CTS_TIMEOUT = POST_SEND_CTS + 1;

    public static int POST_SEND_DATA = COMMON_PRIORITY;
    public static int DATA_TIMEOUT = POST_SEND_DATA + 1;

    public static int POST_SEND_ACK = COMMON_PRIORITY;
    public static int ACK_TIMEOUT = POST_SEND_ACK + 1;

    public static int SEND = COMMON_PRIORITY;
    public static int RECEIVE = SEND + 1;
    public static int AFTER_RECEIVE = RECEIVE + 1;

    @Override
    public int compareTo(TimeTask o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (mTimeToRun != o.mTimeToRun) {
            return mTimeToRun < o.mTimeToRun ? -1 : 1;
        } else if (mPriority != o.mPriority) {
            return mPriority < o.mPriority ? -1 : 1;
        } else if(bornTime != o.bornTime) {
            return bornTime < o.bornTime ? -1 : 1;
        } else{
            return 0;
        }
    }

    TimeTask(double timeToRun , Runnable toRun , int priority){
        this.mTimeToRun = PrecisionUtil.round(timeToRun);
        if(toRun == null) throw new NullPointerException();
        this.mToRun = toRun;
        this.mPriority = priority;
        this.bornTime = serialNum ++;
    }

    double getTaskTime(){
        return mTimeToRun;
    }

    void reduceTime(double time){
        this.mTimeToRun = PrecisionUtil.sub(this.mTimeToRun,time);
    }

    void doTask(){
        mToRun.run();
    }

}
