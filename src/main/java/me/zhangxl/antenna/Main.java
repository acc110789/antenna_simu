package me.zhangxl.antenna;

import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.station.StationUtil;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
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
        List<Station> stationList = new ArrayList<>();
        stationList.add(new Station(1,1,0));
        stationList.add(new Station(2,0,1));
        stationList.add(new Station(3,-1,0));

        //激活所有的站点
        for (Station station : stationList) {
            StationUtil.guaranteeEnoughFrame(station);
        }

        //时间片在主线程中开始流动
        TimeController.getInstance().loop();
    }

    private static void checkConfig() {
        System.out.println("start check config");
        String errInfo = "";
        if (Config.getInstance().getStationNum() <= 0) {
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
        if(Config.getInstance().getWarmUp() <= 0){
            errInfo += "    warm up time is less than 0";
        }
        //warp up的时间不能太长,即不能超过仿真时间的一半
        if(Config.getInstance().getWarmUp() >= Config.getInstance().getSimulationDuration()/2){
            errInfo += "    warm up time is larger than a half of simulation duration";
        }
        if(Config.getInstance().getRealDataLength() <= 0){
            errInfo += "    fix data length is less than 0";
        }
        if(Config.getInstance().getAntennaMode() != Medium.DIRECT_MODE
                && Config.getInstance().getAntennaMode() != Medium.OMNI_MODE){
            throw new RuntimeException("incorrect mode");
        }
        int part = Config.getInstance().getPart();
        if(part <= 1 || part == 7 || part == 11 || part > 12){
            throw new RuntimeException("incorrect part num");
        }
        if(!errInfo.isEmpty()){
            throw new IllegalArgumentException(errInfo);
        }
    }
}
