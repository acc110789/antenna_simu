package me.zhangxl.antenna.application;

import me.zhangxl.antenna.infrastructure.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * App可以理解为一个用户或者可以理解为上层的应用程序,
 * 一个User持有一个Station,
 * User可以每隔一段时间以一定的规律发送给另外一个User request
 * Created by zhangxiaolong on 16/3/24.
 */
public abstract class App {

    private final Station mStation;

    private static final List<Integer> stationIds = new ArrayList<>();

    App(int id){
        mStation = new Station(id);
        stationIds.add(id);
    }

    private Station getStation(){
        return mStation;
    }

    public void loop(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getStation().sendRequest(getNextTargetId(),getNextDataRequestLength());
                loop();
            }
        },getNextTimePoint());
    }

    protected abstract long getNextTimePoint();

    protected abstract long getNextDataRequestLength();

    protected abstract int getNextTargetId();

    protected List<Integer> getAllIds(){
        return new ArrayList<>(stationIds);
    }
}
