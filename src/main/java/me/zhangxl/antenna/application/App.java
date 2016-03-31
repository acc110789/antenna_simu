package me.zhangxl.antenna.application;

import me.zhangxl.antenna.infrastructure.ClockController;
import me.zhangxl.antenna.infrastructure.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * App可以理解为一个用户或者可以理解为上层的应用程序,
 * 一个App持有一个Station,App可以每隔一段时间以一定
 * 的规律发送给随机的另一个Station发送随机长度的DataFrame
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
            ClockController.getInstance().post(getRunnable(), getNextFrameInterval());
        }
    }

    public void deActivate(){
        active.set(false);
    }

    /**
     * @return 当前DataFrame和下一个DataFrame之间的
     * 间隔时间,亦即产生了当前DataFrame并把这个Frame交
     * 给Station之后,还需要多久时间产生下一个DataFrame
     * 并交给Station发送
     */
    protected abstract long getNextFrameInterval();

    /**
     * @return 下一个DataFrame的长度
     */
    protected abstract long getNextFrameLength();

    /**
     * @return 下一个DataFrame的传输目标
     */
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
                    ClockController.getInstance().post(getRunnable(), getNextFrameInterval());
                }
            }
        };
    }
}
