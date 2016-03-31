package me.zhangxl.antenna.infrastructure.clock;

import me.zhangxl.antenna.util.Logger;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 负责时间槽的移动.
 * 所有需要用到时间的地方,比如Station或者Medium都要监听这个时间槽
 * Created by zhangxiaolong on 16/3/29.
 */
public class ClockController {

    private  Logger logger = new Logger(ClockController.class,false);

    private PriorityBlockingQueue<ClockTask> tasks = new PriorityBlockingQueue<>();

    private AtomicBoolean active = new AtomicBoolean(true);

    private Runnable loopCallBack;

    private ClockController(){}

    private static final ClockController sInstance = new ClockController();

    public static ClockController getInstance(){
        return sInstance;
    }

    /**同步的目的是保证不会被乱添加,
     * 只能在一开始活着loop循环调用的函数中添加.
     * 防止刚刚被添加进来时间就被剪掉
     */
    public synchronized void post(Runnable toRun,float timeToRun){
        if(Logger.DEBUG_CLOCK){
            logger.log("post a runnable at %f",timeToRun);
        }
        tasks.put(new ClockTask(timeToRun,toRun));
    }

    public void deActive(){
        active.set(false);
    }

    public synchronized void loop() throws InterruptedException {
        onLoop();
        while (active.get()) {
            ClockTask task = tasks.take();
            //减去时间
            float time = task.getTaskTime();
            if (Logger.DEBUG_CLOCK) {
                logger.log("time has passed : %f", time);
            }
            task.reduceTime(time);
            for (ClockTask task1 : tasks) {
                task1.reduceTime(time);
            }
            //下面开始执行任务
            if (Logger.DEBUG_CLOCK) {
                logger.log("do a task...");
            }
            doTask(task);
            while (true) {
                //搜索可能还需要执行的任务
                ClockTask temp = tasks.peek();
                if (temp != null && Math.abs(temp.getTaskTime()) < 1e-10) { //浮点数比较,当绝对值小于一定值,可以认为是0
                    if (Logger.DEBUG_CLOCK) {
                        logger.log("do a same time task...");
                    }
                    doTask(temp);
                } else {
                    //说明没有task了 或者需要等到下一个时间点运行
                    break;
                }
            }
        }
    }

    private void doTask(ClockTask task){
        task.doTask();
    }

    private void onLoop(){
        if(loopCallBack != null){
            loopCallBack.run();
        }
    }

    public void setLoopCallBack(Runnable callBack){
        this.loopCallBack = callBack;
    }
}
