package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 本来应该是每一个Station都有一个MediumObserver.
 * 但是本仿真程序假设已经做好了同步.
 * 因此所有的Station都用的是同一个MediumObserver.
 * MediumObservers 仅仅被Station持有.
 *
 * MediumObservers 需要做的事情:
 * 通知相应的Station将当前的DataFrame的backOff计数器减1
 *
 * 问题:谁来通知MediumObserver呢? 由Medium来通知
 *  {@link Medium}
 * Created by zhangxiaolong on 16/3/30.
 */
public class MediumObservers implements MediumObserver {

    private static Logger logger = new Logger(MediumObservers.class);

    private static MediumObservers ourInstance = new MediumObservers();

    private List<MediumObserver> observers = new ArrayList<>();

    public static MediumObservers getInstance() {
        return ourInstance;
    }

    public synchronized void register(MediumObserver observer){
        if(!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    private synchronized MediumObserver[] getObservers(){
        return observers.toArray(new MediumObserver[observers.size()]);
    }

    @Override
    public void onRtsCollision(List<RtsFrame> collisionFrames) {
        for(MediumObserver observer : getObservers()){
            observer.onRtsCollision(collisionFrames);
        }
    }

    @Override
    public void onNewSLot() {
        if(Logger.DEBUG_STATION){
            logger.log("onNewSlot...");
        }
        for(MediumObserver observer : getObservers()){
            observer.onNewSLot();
        }
        Medium.getInstance().checkCollisionAndSend();
    }

    @Override
    public void onPostDifs() {
        if(Logger.DEBUG_STATION){
            logger.log("onDIFS...");
        }
        for(MediumObserver observer : getObservers()){
            observer.onPostDifs();
        }
        Medium.getInstance().checkCollisionAndSend();
    }
}
