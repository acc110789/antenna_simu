package me.zhangxl.antenna.infrastructure.clock;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;
import me.zhangxl.antenna.util.PrecisionUtil;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 在仿真时间内要统计的东西:
 * (1)总共的数据传输量,单位是bytes
 * (2)碰撞概率   碰撞次数除以发送的次数
 *
 * 负责时间槽的移动.
 * 所有需要用到时间的地方,比如Station或者Medium都要监听这个时间槽
 * Created by zhangxiaolong on 16/3/29.
 */
public class TimeController {

    private static final TimeController sInstance = new TimeController();
    private  Logger logger = new Logger(TimeController.class,false);
    private PriorityBlockingQueue<TimeTask> tasks = new PriorityBlockingQueue<>();
    private AtomicBoolean active = new AtomicBoolean(true);
    private Runnable loopCallBack;
    private double currentTime = 0;

    /**
     * 统计的阀门,只有当持续时间过了warmUp时间之后,才真的开始统计
     */
    private boolean statValve = false;
    /**
     * 统计总的数据量(bytes)
     */
    private long totalDataAmount = 0;
    /**
     * 总的碰撞次数
     */
    private long totalCollitionTimes = 0;
    /**
     * 总的发送次数
     */
    private long totalSendTimes = 0;

    private TimeController(){}

    public static TimeController getInstance(){
        return sInstance;
    }

    public double getCurrentTime(){
        return this.currentTime;
    }

    /**同步的目的是保证不会被乱添加,
     * 只能在一开始活着loop循环调用的函数中添加.
     * 防止刚刚被添加进来时间就被剪掉
     */
    public  void post(Runnable toRun,double timeToRun){
        if(Logger.DEBUG_CLOCK){
            logger.log("post a runnable at %f",timeToRun);
        }
        tasks.put(new TimeTask(timeToRun,toRun,TimeTask.COMMON_PRIORITY));
    }

    public  void post(Runnable toRun,double timeToRun,int priority){
        if(Logger.DEBUG_CLOCK){
            logger.log("post a runnable at %f",timeToRun);
        }
        tasks.put(new TimeTask(timeToRun,toRun,priority));
    }

    public void deActive(){
        active.set(false);
    }

    public void addCollitionTimes(){
        if(statValve){
            totalCollitionTimes ++;
        }
    }

    public void addSendTimes(){
        if(statValve){
            totalSendTimes ++;
        }
    }

    public void addDataAmount(long length){
        if(statValve){
            totalDataAmount += length;
        }
    }

    public synchronized void loop() throws InterruptedException {
        preLoop();
        while (active.get() && this.currentTime < Config.getInstance().getSimulationDuration()) {
            TimeTask task = tasks.take();
            //减去时间
            double time = task.getTaskTime();
            if (Logger.DEBUG_CLOCK) {
                logger.log("time has passed : %f", time);
            }
            task.reduceTime(time);
            for (TimeTask task1 : tasks) {
                task1.reduceTime(time);
            }
            //仿真过程积累相应的时间

            currentTime = PrecisionUtil.add(currentTime,time);
            if(currentTime != PrecisionUtil.round(currentTime)){
                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
            }
            if(time > 0){
                logger.logln();
            }

            if(!statValve && currentTime >= Config.getInstance().getWarmUp()){
                statValve = true;
            }
            //下面开始执行任务
            if (Logger.DEBUG_CLOCK) {
                logger.log("do a task...");
            }
            task.doTask();
            while (true) {
                //搜索可能还需要执行的任务
                TimeTask temp = tasks.peek();
                if (temp != null && temp.getTaskTime() == 0) {
                    if (Logger.DEBUG_CLOCK) {
                        logger.log("do a same time task...");
                    }
                    temp = tasks.poll();
                    temp.doTask();
                } else {
                    //说明没有task了 或者需要等到下一个时间点运行
                    break;
                }
            }
        }
        postLoop();
    }

    private void postLoop(){
        System.out.println("*************************************************");
        System.out.println("*************************************************");
        System.out.format("总的数据传输量(bytes): %d", totalDataAmount);
        System.out.println();
        System.out.format("总的发送次数: %d",totalSendTimes);
        System.out.println();
        System.out.format("总的碰撞次数: %d",totalCollitionTimes);
        System.out.println();
        System.out.format("碰撞发生的概率: %f",PrecisionUtil.div((totalCollitionTimes+0.0),totalSendTimes));
        System.out.println();
        System.out.println("*************************************************");
    }

    private void preLoop(){
        if(loopCallBack != null){
            loopCallBack.run();
        }
    }

    public void setLoopCallBack(Runnable callBack){
        this.loopCallBack = callBack;
    }
}
