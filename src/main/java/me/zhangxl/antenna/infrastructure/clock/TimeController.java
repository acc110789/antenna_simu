package me.zhangxl.antenna.infrastructure.clock;

import me.zhangxl.antenna.util.*;

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

    private void printTimeHint(double time){
        if(time > 0){
            logger.unLogHeader();
            String text;
            if(PrecisionUtil.sub(time, Config.getDifs()) == 0){
                text = "DIFS";
            } else if(PrecisionUtil.sub(time, Config.getSifs()) == 0){
                text = "SIFS";
            } else if(PrecisionUtil.sub(time, Config.getSlotLength()) == 0){
                text = "SLOT";
            } else if(PrecisionUtil.sub(time, Config.getEifs()) == 0){
                text = "EIFS";
            } else if(PrecisionUtil.sub(time, Constant.getRtsTimeLength()) == 0){
                text = "RTS";
            } else if(PrecisionUtil.sub(time, Constant.getCtsTimeLength()) == 0){
                text = "CTS/ACK";
            } else if(PrecisionUtil.sub(time, Constant.getDataTimeLength()) == 0){
                text = "DATA";
            }  else if(PrecisionUtil.sub(time,Constant.getCtsTimeOut()) == 0){
                text = "CTS/ACK timeout";
            } else if(PrecisionUtil.sub(time, Constant.getBofFrameTimeLength()) == 0){
                text = "Bof";
            } else if(PrecisionUtil.sub(time, Constant.getOtcFrameTimeLength()) == 0){
                text = "Otc";
            } else {
                text = String.format("%#.14f", time);
            }
            logger.trace(TimeLogger.headerFormatter,text,"passed");
            logger.logHeader();
        }
    }

    public synchronized void loop() throws InterruptedException {
        preLoop();
        while (active.get() && this.currentTime < Config.getSimulationDuration()) {
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
            printTimeHint(time);

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
        Statistic.printResult();
    }

    private void preLoop(){
        if(loopCallBack != null){
            loopCallBack.run();
        }
    }

    public void clear(){
        tasks.clear();
        currentTime = 0.0;
    }

    public void setLoopCallBack(Runnable callBack){
        this.loopCallBack = callBack;
    }
}
