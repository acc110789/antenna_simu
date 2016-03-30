package me.zhangxl.antenna.application;

import me.zhangxl.antenna.infrastructure.ClockController;
import me.zhangxl.antenna.infrastructure.ClockObserver;
import me.zhangxl.antenna.infrastructure.Station;

import java.util.ArrayList;
import java.util.List;

/**
 * App可以理解为一个用户或者可以理解为上层的应用程序,
 * 一个User持有一个Station,
 * User可以每隔一段时间以一定的规律发送给另外一个User request
 * Created by zhangxiaolong on 16/3/24.
 */
public abstract class App implements ClockObserver {

    private final Station mStation;

    private Boolean active;

    private static final List<Integer> stationIds = new ArrayList<>();

    App(int id){
        mStation = new Station(id);
        stationIds.add(id);
        ClockController.getInstance().register(this);
    }

    private Station getStation(){
        return mStation;
    }

    public void activate(){
        synchronized (active) {
            active = true;
        }
    }

    public void deActivate(){
        synchronized (active) {
            active = false;
        }
    }

    protected abstract long getNextSlotNum();

    protected abstract long getNextFrameLength();

    protected abstract int getNextDesId();

    protected List<Integer> getAllIds(){
        return new ArrayList<>(stationIds);
    }

    @Override
    public void onNewSlot() {
        synchronized (active) {
            if(active) {
                // TODO: 16/3/29 计算每一个slot产生一个frame的概率
                getStation().sendRequest(getNextDesId(), getNextFrameLength());
            }
        }
    }
}
