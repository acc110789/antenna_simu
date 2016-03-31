package me.zhangxl.antenna.application;

import me.zhangxl.antenna.infrastructure.ClockController;
import me.zhangxl.antenna.infrastructure.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * App可以理解为一个用户或者可以理解为上层的应用程序,
 * 一个User持有一个Station,
 * User可以每隔一段时间以一定的规律发送给另外一个User request
 * Created by zhangxiaolong on 16/3/24.
 */
public abstract class App  {

    private final Station mStation;

    private AtomicBoolean active = new AtomicBoolean(false);

    private static final List<Integer> stationIds = new ArrayList<>();

    App(int id){
        mStation = new Station(id);
        stationIds.add(id);
    }

    public Station getStation(){
        return mStation;
    }

    public void activate(){
        if(!active.get()) {
            active.set(true);
            ClockController.getInstance().post(getRunnable(),getNextFrameTime());
        }
    }

    public void deActivate(){
        active.set(false);
    }

    protected abstract long getNextFrameTime();

    protected abstract long getNextFrameLength();

    protected abstract int getNextDesId();

    protected List<Integer> getAllIds(){
        return new ArrayList<>(stationIds);
    }

    private Runnable getRunnable(){
        return new Runnable() {
            @Override
            public void run() {
                if(active.get()){
                    getStation().sendRequest(getNextDesId(), getNextFrameLength());
                    ClockController.getInstance().post(getRunnable(),getNextFrameTime());
                }
            }
        };
    }
}
