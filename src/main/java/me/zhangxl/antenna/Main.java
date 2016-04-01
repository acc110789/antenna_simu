package me.zhangxl.antenna;

import me.zhangxl.antenna.application.App;
import me.zhangxl.antenna.application.ExpApp;
import me.zhangxl.antenna.infrastructure.clock.ClockController;
import me.zhangxl.antenna.util.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * 暂时先做计算机局域网络的仿真(即单信道,全方向发送,全方向接受)
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        checkConfig();

        //新建站点
        List<App> appList = new ArrayList<>();
        for (int i = 0; i < Config.getInstance().getUserNum(); i++) {
            App app = new ExpApp(i);

            appList.add(app);
        }

        //激活所有的站点
        for (App app : appList) {
            app.activate();
        }

        //时间片在主线程中开始流动
        ClockController.getInstance().loop();
    }

    private static void checkConfig() {
        String errInfo = "";
        if (Config.getInstance().getUserNum() <= 0) {
            errInfo += "    userNum is negative";
        }
        if(Config.getInstance().getDifs() <= Config.getInstance().getSifs()){
            errInfo += "    difs is less than or equal sifs";
        }
        if(Config.getInstance().getSifs() <= 0){
            errInfo += "    sifs is less than or equal 0";
        }
        if(Config.getInstance().getSlotLength() <= 0){
            errInfo += "    slotlength is less than or equal 0";
        }
        if(Config.getInstance().getMaxCW() <= Config.getInstance().getDefaultCW()){
            errInfo += "    maxCW is less than or equal defaultCW";
        }
        if(Config.getInstance().getDefaultCW() < 0){
            errInfo += "    defaultCW is less than 0";
        }
        if(Config.getInstance().getPhyHeader() <= 0){
            errInfo += "    phyHeader is less than or equal 0";
        }
        if(Config.getInstance().getMacHeader() <= 0){
            errInfo += "    macHeader is less than or equal 0";
        }
        if(Config.getInstance().getMacRtsHeader() <= 0){
            errInfo += "    macRtsHeader is less than or equal 0";
        }
        if(Config.getInstance().getRtsLength() <= 0){
            errInfo += "    rtsLength is less than or equal 0";
        }
        if(Config.getInstance().getCtsLength() <= 0){
            errInfo += "    ctsLength is less than or equal 0";
        }
        if(Config.getInstance().getAckLength() <= 0){
            errInfo += "    ackLength is less than or equal 0";
        }
        if(Config.getInstance().getBandWidth() <= 0){
            errInfo += "    bandwidth is less than or equal 0";
        }
        if(Config.getInstance().getSimulationDuration() <= 0){
            errInfo += "    simulationDuration less than or equal 0";
        }
        if(!errInfo.isEmpty()){
            throw new IllegalArgumentException(errInfo);
        }
    }
}
