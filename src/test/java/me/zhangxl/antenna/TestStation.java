package me.zhangxl.antenna;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.clock.ClockController;
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

        station1.sendRequest(2, 400);
        station2.sendRequest(1, 400);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 9; i++) {
                    station1.sendRequest(2, 400);
                    station2.sendRequest(1, 400);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        Class<Config> configClass = Config.class;
        Field field = configClass.getDeclaredField("simulationDuration");
        field.setAccessible(true);
        field.setFloat(Config.getInstance(), (float) 3e-2);
        ClockController.getInstance().loop();
    }
}
