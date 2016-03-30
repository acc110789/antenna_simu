package me.zhangxl.antenna;

import me.zhangxl.antenna.application.App;
import me.zhangxl.antenna.application.ExpApp;
import me.zhangxl.antenna.infrastructure.ClockController;

import java.util.ArrayList;
import java.util.List;

/**
 * 暂时先做计算机局域网络的仿真(即单信道,全方向发送,全方向接受)
 */
public class Main {

    private static final List<App> appList = new ArrayList<>();


    public static void main(String[] args) throws InterruptedException {
        checkConfig();

        //新建站点

        for (int i = 0; i < Config.UserNum; i++) {
            App app = new ExpApp(i);
            appList.add(app);
        }
        //启动所有的站点
        for(App app : appList){
            app.activate();
        }

        //时间片开始流动
        ClockController.getInstance().loop();
    }

    private static void checkConfig() {
        if (Config.UserNum <= 0) {
            throw new IllegalArgumentException("user num is negative");
        }
    }
}
