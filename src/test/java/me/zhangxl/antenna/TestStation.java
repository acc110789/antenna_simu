package me.zhangxl.antenna;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.infrastructure.pcp.PcpStation;
import me.zhangxl.antenna.util.*;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * 测试Station之间的交互
 * Created by zhangxiaolong on 16/3/31.
 */
public class TestStation {

    private static final TimeLogger logger = SimuLoggerManager.getLogger(TestStation.class.getSimpleName());

    /**
     * @param time 设置计时起点
     */
    private void setWarmUpTime(double time) throws NoSuchFieldException, IllegalAccessException {
        Config.getInstance();
        Class<Config> configClass = Config.class;
        Field field2 = configClass.getDeclaredField("warmUp");
        field2.setAccessible(true);
        field2.setDouble(Config.getInstance(), time);
    }

    private void setSimulationDuration(double time) throws NoSuchFieldException, IllegalAccessException {
        Config.getInstance();
        Class<Config> configClass = Config.class;
        Field field1 = configClass.getDeclaredField("simulationDuration");
        field1.setAccessible(true);
        field1.setDouble(Config.getInstance(), time);
    }

    /**
     * 定向天线测试
     * @throws IllegalAccessException
     * @throws InterruptedException
     * @throws NoSuchFieldException
     */
    @Test
    public void threeDirectStation() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        printConfig();
        setSimulationDuration(0.1);
        setWarmUpTime(0);
        Medium.reset();
        TimeController.getInstance().clear();

        Station station1 = new Station(1, 1, 0);
        Station station2 = new Station(2, 0, 1);
        Station station3 = new Station(3, -1, 0);
        PcpStation.getInstance();

        StationUtil.guaranteeEnoughFrame(station1);
        StationUtil.guaranteeEnoughFrame(station2);
        StationUtil.guaranteeEnoughFrame(station3);

        TimeController.getInstance().loop();
    }

    private void printConfig() {
        logger.unLogHeader();
        logger.info("**************************************");
        logger.info("***************config*****************");
        logger.ln();
        logger.info("%-13s%#.14f", "slot length", Config.getInstance().getSlotLength());
        logger.info("%-13s%#.14f", "rts  length", Constant.getRtsTimeLength());
        logger.info("%-13s%#.14f", "difs length", Config.getInstance().getDifs());
        logger.info("%-13s%#.14f", "cts  timeout", Constant.getCtsTimeOut());
        logger.info("%-13s%#.14f", "eifs length", Config.getInstance().getEifs());
        logger.info("%-13s%#.14f", "next length", Constant.getBofFrameTimeLength());
        logger.ln();
        logger.info("**************************************");
        logger.info("**************************************");
        logger.ln();
        logger.ln();
        logger.logHeader();
    }

    @Test
    public void testDouble() {
        System.out.println(PrecisionUtil.round(Math.PI));
    }

}
