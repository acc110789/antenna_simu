package me.zhangxl.antenna;

import me.zhangxl.antenna.infrastructure.clock.ClockController;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * ClockController功能是否正确
 * Created by zhangxiaolong on 16/3/30.
 */
public class TestClockController {

    @Test
    public void clockController() throws InterruptedException {
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 10; i++) {
            ClockController.getInstance().post(new Runnable() {
                @Override
                public void run() {

                }
            },random.nextFloat());
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ClockController.getInstance().loop();
                    Assert.fail("program can not exit from here");
                } catch (InterruptedException ignored) {
                }
            }
        });
        thread.start();
        Thread.sleep(500);
        ClockController.getInstance().deActive();
        if(thread.isAlive()){
            thread.interrupt();
        }
        thread.join();
    }
}
