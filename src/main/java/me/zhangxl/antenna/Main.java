package me.zhangxl.antenna;

import me.zhangxl.antenna.application.App;
import me.zhangxl.antenna.application.ExpApp;
import me.zhangxl.antenna.infrastructure.ClockController;
import me.zhangxl.antenna.infrastructure.Medium;
import me.zhangxl.antenna.util.Config;

/**
 * 暂时先做计算机局域网络的仿真(即单信道,全方向发送,全方向接受)
 */
public class Main {


    public static void main(String[] args) throws InterruptedException {
        checkConfig();

        //新建站点
        for (int i = 0; i < Config.UserNum; i++) {
            App app = new ExpApp(i);
            Medium.appList.add(app);
        }

        //激活所有的站点
        for(App app : Medium.appList){
            app.activate();
        }

        //时间片在主线程中开始流动
        ClockController.getInstance().loop();
    }

    private static void checkConfig() {
        if (Config.UserNum <= 0) {
            throw new IllegalArgumentException("user num is negative");
        }
    }
}
