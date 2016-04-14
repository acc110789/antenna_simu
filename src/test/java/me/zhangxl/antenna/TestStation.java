package me.zhangxl.antenna;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * 测试Station之间的交互
 * Created by zhangxiaolong on 16/3/31.
 */
public class TestStation {

    @Test
    public void twoStation() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        final Station station1 = new Station(1);
        final Station station2 = new Station(2);

        for (int i = 0; i < 10; i++) {
            station1.putDataFrame(2, 400);
            station2.putDataFrame(1, 400);
        }
        Config.getInstance();
        Class<Config> configClass = Config.class;
        Field field1 = configClass.getDeclaredField("simulationDuration");
        field1.setAccessible(true);
        field1.setDouble(Config.getInstance(), 3e-2);

        Field field2 = configClass.getDeclaredField("warmUp");
        field2.setAccessible(true);
        field2.setDouble(Config.getInstance(), 1e-2);
        TimeController.getInstance().loop();
    }

    @Test
    public void leftFormatString(){
        ppformat("%d king is king",12222);
    }

    private void ppformat(String formatter,Object... objects){
        System.out.format(String.format("%-15s","ppformat") + formatter,objects);
    }
}
