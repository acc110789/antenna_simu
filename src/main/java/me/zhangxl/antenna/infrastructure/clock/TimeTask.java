package me.zhangxl.antenna.infrastructure.clock;

/**
 *  {@link TimeController} 持有的ClockTask
 * Created by zhangxiaolong on 16/3/30.
 */
class TimeTask implements Comparable<TimeTask> {

    private double mTimeToRun;
    private final Runnable mToRun;
    private final long bornTime;
    private static long serialNum = 1;

    @Override
    public int compareTo(TimeTask o) {
        if(o == null){
            throw new NullPointerException();
        }
        if(mTimeToRun != o.mTimeToRun){
            return mTimeToRun < o.mTimeToRun ? -1 : 1;
        } else if(this != o){
            return bornTime < o.bornTime ? -1 : 1;
        } else {
            return 0;
        }

    }

    TimeTask(double timeToRun, Runnable toRun) {
        this.mTimeToRun = timeToRun;
        if(toRun == null) throw new NullPointerException();
        this.mToRun = toRun;
        this.bornTime = serialNum ++;
    }

    double getTaskTime(){
        return mTimeToRun;
    }

    void reduceTime(double time){
        this.mTimeToRun -= time;
    }

    void doTask(){
        mToRun.run();
    }

}
