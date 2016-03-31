package me.zhangxl.antenna;

import me.zhangxl.antenna.infrastructure.clock.ClockController;
import me.zhangxl.antenna.infrastructure.Station;
import org.junit.Test;

/**
 * 测试Station之间的交互
 * Created by zhangxiaolong on 16/3/31.
 */
public class TestStation {

    @Test
    public void twoStation() throws InterruptedException {
        Station station1 = new Station(1);
        Station station2 = new Station(2);

        station1.sendRequest(2,400);
        station2.sendRequest(1,400);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ClockController.getInstance().loop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
//        for(int i=0;i<10;i++){
//            station1.sendRequest(2,800);
//        }
//        for(int i=0;i<10;i++){
//            station2.sendRequest(1,800);
//        }
        Thread.sleep(20);
    }
}
