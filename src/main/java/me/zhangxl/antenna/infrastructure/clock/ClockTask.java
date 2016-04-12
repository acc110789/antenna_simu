package me.zhangxl.antenna.infrastructure.clock;

/**
 *  {@link TimeController} 持有的ClockTask
 * Created by zhangxiaolong on 16/3/30.
 */
class ClockTask implements Comparable<ClockTask> {

    private double mTimeToRun;
    private final Runnable mToRun;

    @Override
    public int compareTo(ClockTask o) {
        if(o == null){
            throw new NullPointerException();
        }
        return (mTimeToRun - o.mTimeToRun) > 0 ? 1 : -1;
    }

    ClockTask(double timeToRun, Runnable toRun) {
        this.mTimeToRun = timeToRun;
        if(toRun == null) throw new NullPointerException();
        this.mToRun = toRun;
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
