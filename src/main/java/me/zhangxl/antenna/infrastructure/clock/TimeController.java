package me.zhangxl.antenna.infrastructure.clock;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;
import me.zhangxl.antenna.util.SimuLoggerManager;
import me.zhangxl.antenna.util.TimeLogger;

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
    private TimeLogger logger = SimuLoggerManager.getLogger(TimeController.class.getSimpleName());
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
        tasks.put(new TimeTask(timeToRun,toRun,TimeTask.COMMON_PRIORITY));
    }

    public  void post(Runnable toRun,double timeToRun,int priority){
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
            if (TimeLogger.DEBUG_CLOCK) {
                logger.debug("time has passed : %f", time);
            }
            task.reduceTime(time);
            for (TimeTask task1 : tasks) {
                task1.reduceTime(time);
            }
            //仿真过程积累相应的时间

            currentTime = PrecisionUtil.add(currentTime,time);
            //检查点
            if(currentTime != PrecisionUtil.round(currentTime)){
                logger.error("double add error");
            }
            if(time > 0){
                logger.ln();
            }

            if(!statValve && currentTime >= Config.getInstance().getWarmUp()){
                statValve = true;
            }
            //下面开始执行任务
            if (TimeLogger.DEBUG_CLOCK) {
                logger.debug("do a task...");
            }
            task.doTask();
            while (true) {
                //搜索可能还需要执行的任务
                TimeTask temp = tasks.peek();
                if (temp != null && temp.getTaskTime() == 0) {
                    if (TimeLogger.DEBUG_CLOCK) {
                        logger.debug("do a same time task...");
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
        logger.unLogHeader();
        logger.ln();
        logger.ln();
        logger.info("*************************************************");
        logger.info("*************************************************");
        logger.info("成功的数据传输量(bytes): %d", totalDataAmount);
        logger.ln();
        logger.info("总的发送次数: %d",totalSendTimes);
        logger.ln();
        logger.info("总的碰撞次数: %d",totalCollitionTimes);
        logger.ln();
        logger.info("碰撞发生的概率: %f",PrecisionUtil.div((totalCollitionTimes+0.0),totalSendTimes));
        logger.ln();
        logger.info("*************************************************");
        logger.info("*************************************************");
        logger.logHeader();
    }

    private void preLoop(){
        if(loopCallBack != null){
            loopCallBack.run();
        }
    }

    public void clear(){
        tasks.clear();
        currentTime = 0.0;
        statValve = false;
        totalDataAmount = 0;
        totalCollitionTimes = 0;
        totalSendTimes = 0;
    }

    public void setLoopCallBack(Runnable callBack){
        this.loopCallBack = callBack;
    }
}
