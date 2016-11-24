package me.zhangxl.antenna;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.infrastructure.pcp.PcpStation;
import me.zhangxl.antenna.util.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 暂时先做计算机局域网络的仿真(即单信道,全方向发送,全方向接受)
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        checkConfig();
        //坐标生成器
        Generator<Pair<Double, Double>> generator = new CoordinateGenerator();
        List<Station> stations = new ArrayList<>();
        PcpStation.getInstance();
        for (int i = 1; i <= Config.getStationNum(); i++) {
            stations.add(new Station(i, generator.next()));
        }
        //激活所有的站点
        for (Station station : stations) {
            StationUtil.guaranteeEnoughFrame(station);
        }
        //时间片在主线程中开始流动
        TimeController.getInstance().loop();
    }

    private static void checkConfig() {
        System.out.println("start check config");
        String errInfo = "";
        if (Config.getStationNum() <= 0) {
            errInfo += "    userNum is negative";
        }
        if (Config.getDifs() <= Config.getSifs()) {
            errInfo += "    difs is less than or equal sifs";
        }
        if (Config.getSifs() <= 0) {
            errInfo += "    sifs is less than or equal 0";
        }
        if (Config.getSlotLength() <= 0) {
            errInfo += "    slotlength is less than or equal 0";
        }
        if (Config.getMaxCW() <= Config.getDefaultCW()) {
            errInfo += "    maxCW is less than or equal defaultCW";
        }
        if (Config.getDefaultCW() < 0) {
            errInfo += "    defaultCW is less than 0";
        }
        if (Config.getPhyHeader() <= 0) {
            errInfo += "    phyHeader is less than or equal 0";
        }
        if (Config.getMacHeader() <= 0) {
            errInfo += "    macHeader is less than or equal 0";
        }
        if (Config.getRtsLength() <= 0) {
            errInfo += "    rtsLength is less than or equal 0";
        }
        if (Config.getCtsLength() <= 0) {
            errInfo += "    ctsLength is less than or equal 0";
        }
        if (Config.getAckLength() <= 0) {
            errInfo += "    ackLength is less than or equal 0";
        }
        if (Config.getBandWidth() <= 0) {
            errInfo += "    bandwidth is less than or equal 0";
        }
        if (Config.getSimulationDuration() <= 0) {
            errInfo += "    simulationDuration less than or equal 0";
        }
        if (Config.getWarmUp() <= 0) {
            errInfo += "    warm up time is less than 0";
        }
        //warp up的时间不能太长,即不能超过仿真时间的一半
        if (Config.getWarmUp() > Config.getSimulationDuration()) {
            errInfo += "    warm up time is larger than simulation duration";
        }
        if (Config.getPayLoad() <= 0) {
            errInfo += "    fix data length is less than 0";
        }
        if (Config.getAntennaMode() != Medium.DIRECT_MODE
                && Config.getAntennaMode() != Medium.OMNI_MODE) {
            throw new RuntimeException("incorrect mode");
        }
        int part = Config.getPart();
        if (part <= 1 || part == 7 || part == 11 || part > 12) {
            throw new RuntimeException("incorrect part num");
        }
        if (!errInfo.isEmpty()) {
            throw new IllegalArgumentException(errInfo);
        }
    }
}
