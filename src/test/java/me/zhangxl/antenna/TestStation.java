package me.zhangxl.antenna;

import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.StationUtil;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试Station之间的交互
 * Created by zhangxiaolong on 16/3/31.
 */
public class TestStation {

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
        testStations(2,0,1e-1);
    }

    private void printConfig(){
        System.out.println("**************************************");
        System.out.println("***************config*****************");
        System.out.format("slot length :%f",Config.getInstance().getSlotLength());
        System.out.println();
        System.out.format("rts length  :%f",new RtsFrame(-1,-1).getTransmitDuration());
        System.out.println();
        System.out.println("**************************************");
        System.out.println("**************************************");
        System.out.println();
        System.out.println();
    }

    @Test
    public void testThreeStations() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        printConfig();
        testStations(3,0,2e-1);
    }

}
