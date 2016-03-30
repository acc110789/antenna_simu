package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.Config;
import me.zhangxl.antenna.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制着时间槽的移动.
 * 所有需要用到时间的地方,比如Station或者Medium都要监听这个时间槽
 * 等所有的监听者把这个时间点该做的事情做完了,需要通知controller要做的事情完了.
 * Created by zhangxiaolong on 16/3/29.
 */
public class ClockController {

    private Logger logger = new Logger(ClockController.class);

    private List<ClockObserver> observers = new ArrayList<>();

    private long currentTime = 0;

    private ClockController(){}

    private static final ClockController sInstance = new ClockController();

    public static ClockController getInstance(){
        return sInstance;
    }

    public synchronized void register(ClockObserver observer){
        observers.add(observer);
    }

    public synchronized void unregister(ClockObserver observer){
        observers.remove(observer);
    }

    private synchronized ClockObserver[] getObservers(){
        return observers.toArray(new ClockObserver[observers.size()]);
    }

    public void loop(){
        while (observers.size() > 0) {
            currentTime += Config.SLOT_LENGTH;
            if(Logger.LOG_CLOCK){
                logger.log("new clock begin,current time :%d",currentTime);
            }
            for (ClockObserver observer : getObservers()) {
                if(observer != null) {
                    observer.onNewSlot();
                }
            }
        }
    }

}
