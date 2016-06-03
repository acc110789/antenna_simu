package me.zhangxl.antenna.infrastructure.clock;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;
import me.zhangxl.antenna.util.SimuLoggerManager;
import me.zhangxl.antenna.util.TimeLogger;

/**
 * 一些实验的统计数据
 * Created by zhangxiaolong on 16/6/3.
 */
public class Statistic {
    private static TimeLogger logger = SimuLoggerManager.getLogger(TimeController.class.getSimpleName());
    /**
     * 统计总的数据量(bytes)
     */
    private static long totalDataAmount = 0;
    /**
     * 总的碰撞次数
     */
    private static long totalCollitionTimes = 0;
    /**
     * 总的发送次数
     */
    private static long totalSuccessTimes = 0;

    public static void addCollitionTimes(){
        if(canCount()){
            totalCollitionTimes ++;
        }
    }

    public static void addSuccessTimes(){
        if(canCount()){
            totalSuccessTimes++;
        }
    }

    public static void addDataAmount(long length){
        if(canCount()){
            totalDataAmount += length;
        }
    }

    /**
     * @return 当前时刻是否应该进行数据的统计
     */
    private static boolean canCount(){
        double currentTime = TimeController.getInstance().getCurrentTime();
        return PrecisionUtil.largeThan(currentTime, Config.getInstance().getWarmUp());
    }

    public static void printResult(){
        logger.unLogHeader();
        logger.ln();
        logger.ln();
        logger.info("*************************************************");
        logger.info("*************************************************");
        //以M(兆)为单位的数据单位
        double MDataAmount = PrecisionUtil.div(totalDataAmount,1024*1024);
        logger.info("有效的仿真时间: %f",PrecisionUtil.sub(Config.getInstance().getSimulationDuration(),
                Config.getInstance().getWarmUp()));
        logger.info("成功的数据传输量(Mbit): %f", MDataAmount);
        double idealFullDataAmount = PrecisionUtil.mul(Config.getInstance().getBandWidth(),
                PrecisionUtil.sub(Config.getInstance().getSimulationDuration(),Config.getInstance().getWarmUp()));
        logger.info("数据的吞吐比例: %f",PrecisionUtil.div(totalDataAmount,idealFullDataAmount));
        logger.ln();
        long totalAmount = totalSuccessTimes + totalCollitionTimes;
        logger.info("总的发送次数: %d", totalAmount);
        logger.ln();
        logger.info("总的碰撞次数: %d",totalCollitionTimes);
        logger.ln();
        if(totalSuccessTimes > 0) {
            logger.info("碰撞发生的概率: %f", PrecisionUtil.div(totalCollitionTimes, totalAmount));
            logger.ln();
        }
        logger.info("*************************************************");
        logger.info("*************************************************");
        logger.logHeader();
    }

    public static void clear(){
        totalDataAmount = 0;
        totalCollitionTimes = 0;
        totalSuccessTimes = 0;
    }
}
