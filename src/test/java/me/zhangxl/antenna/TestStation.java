package me.zhangxl.antenna;

import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.StationUtil;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
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
        testStations(2,0,0.01);
    }

    private void printConfig(){
        logger.unLogHeader();
        logger.info("**************************************");
        logger.info("***************config*****************");
        logger.ln();
        logger.info("slot length :%#.14f",Config.getInstance().getSlotLength());
        logger.info("rts  length :%#.14f",new RtsFrame(-1,-1).getTransmitDuration());
        logger.info("difs  length :%#.14f",Config.getInstance().getDifs());
        logger.info("cts timeout :%#.14f",CtsFrame.getCtsTimeOut());
        logger.info("rts timeout :%#.14f",RtsFrame.getRtsTimeOut());
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
        testStations(3,0,0.05);
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
