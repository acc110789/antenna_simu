package me.zhangxl.antenna;

import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.StationUtil;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;
import me.zhangxl.antenna.util.SimuLoggerManager;
import me.zhangxl.antenna.util.TimeLogger;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    private List<Station> getStations(int num) {
        List<Station> result = new ArrayList<>();
        for (int i = 1; i <= num; i++) {
            result.add(new Station(i));
        }
        return result;
    }

    private void testStations(int num,double warmUp,double duration)
            throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        for(Station station : getStations(num)){
            StationUtil.guaranteeEnoughFrame(station);
        }
        setSimulationDuration(duration);
        setWarmUpTime(warmUp);
        TimeController.getInstance().loop();
    }

    @Test
    public void twoStation() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        printConfig();
        for(int i=0;i<10;i++) {
            logger.unLogHeader();
            logger.info("第%d次仿真试验", i);
            logger.logHeader();
            logger.ln();
            TimeController.getInstance().clear();
            Medium.getInstance().clear();
            testStations(2, 0, 0.02);
        }
    }

    @Test
    public void twoStationLong() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        printConfig();
        for(int i=0;i<10;i++) {
            logger.unLogHeader();
            logger.info("第%d次仿真试验", i);
            logger.logHeader();
            logger.ln();
            TimeController.getInstance().clear();
            Medium.getInstance().clear();
            testStations(2, 0, 10);
        }
    }



    private void printConfig(){
        logger.unLogHeader();
        logger.info("**************************************");
        logger.info("***************config*****************");
        logger.ln();
        logger.info("%-13s%#.14f","slot length",Config.getInstance().getSlotLength());
        logger.info("%-13s%#.14f","rts  length",new RtsFrame(-1,-1).getTransmitDuration());
        logger.info("%-13s%#.14f","difs length",Config.getInstance().getDifs());
        logger.info("%-13s%#.14f","cts  timeout",CtsFrame.getCtsTimeOut());
        logger.info("%-13s%#.14f","rts  timeout",RtsFrame.getRtsTimeOut());
        logger.info("%-13s%#.14f","eifs length",Config.getInstance().getEifs());
        logger.ln();
        logger.info("**************************************");
        logger.info("**************************************");
        logger.ln();
        logger.ln();
        logger.logHeader();
    }

    @Test
    public void testThreeStations() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        printConfig();
        for(int i=0;i<10;i++) {
            logger.unLogHeader();
            logger.info("第%d次仿真试验", i);
            logger.logHeader();
            logger.ln();
            TimeController.getInstance().clear();
            Medium.getInstance().clear();
            testStations(3, 0, 0.05);
        }
    }

    @Test
    public void testFourStations() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        printConfig();
        testStations(4,0,0.05);
    }

    @Test
    public void testDouble(){
        System.out.println(PrecisionUtil.round(Math.PI));
    }

    @Test
    public void testDoubleCompare(){
        double a = 0;
        double b = PrecisionUtil.round(1/7);
        a += b;
        double c = PrecisionUtil.round(1/11);
        a += c;
        System.out.println(a==(b+c));
        System.out.println(0.05 + 0.01);
        System.out.println(PrecisionUtil.add(0.05,0.01));
        System.out.println(1.0 - 0.42);
        System.out.println(PrecisionUtil.sub(1.0,0.42));
        System.out.println(4.015 * 100);
        System.out.println(PrecisionUtil.mul(4.015,100.0));
        System.out.println(123.3 / 100);
        System.out.println(PrecisionUtil.div(123.3,100.0));

        System.out.println();
        System.out.println();
        System.out.println();
        BigDecimal big = new BigDecimal("12.22");
        BigDecimal big1 = new BigDecimal("12.23");
        big.add(big1);
        System.out.println("big :" + big);
    }

}
