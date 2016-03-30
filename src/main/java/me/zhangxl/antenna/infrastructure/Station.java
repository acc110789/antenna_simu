package me.zhangxl.antenna.infrastructure;

import com.sun.tools.javac.util.Pair;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;
import me.zhangxl.antenna.request.DataFrame;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 该类代表一个站点的状态
 *
 * tips
 * (1):发生碰撞应该是Station自己意识到的
 * Created by zhangxiaolong on 16/3/24.
 */
public class Station {

    private Random random = new Random(System.currentTimeMillis());

    private static final Logger logger = new Logger(Station.class);
    //wait list
    private Queue<DataFrame> mDataRequests = new ConcurrentLinkedDeque<>();

    private final int id;

    private final Pair<Double,Double> mLocation; //定向天线时需要保证

    private boolean isBusy = false;

    private boolean isSending = false;

    private DataFrame currentDataRequest;

    private final Object tifLock = new Object();

    //表明station是否是在tif中,在tif中什么事情也不能做,就等待,这个变量的修改必须是在同步中
    private boolean isTifing = false;
    private List<Runnable> tifRunnables = new ArrayList<>();

    public Station(int id){
        this.id = id;
        mLocation = null;
    }

    public Station(int id,Double xAxis,Double yAxis){
        this.id = id;
        this.mLocation = new Pair<>(xAxis,yAxis);
    }

    private void putRequest(final DataFrame dataRequest){
        // TODO: 16/3/24 有可能是直接处理这个dataRequest,也有可能是把这个request放到list中
        if(isBusy) {
            mDataRequests.add(dataRequest);
        } else {
            //当前可能正在Tif中
            synchronized (tifLock){
                if(isTifing){
                    tifRunnables.add(new Runnable() {
                        @Override
                        public void run() {
                            startTransmit(dataRequest);
                        }
                    });
                } else {
                    startTransmit(dataRequest);
                }
            }
        }
    }

    /**
     * 初始化一个tif
     */
    public void initTif() {
        synchronized (tifLock) {
            if (isTifing) {
                // TODO: 16/3/25 是否应该直接退出
                logger.log("already init a tif");
            } else {
                isTifing = true;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (tifLock) {
                            isTifing = false;
                            for(Runnable toRun : tifRunnables){
                                toRun.run();
                            }
                        }
                    }
                }, Config.tifDuration);
            }
        }
    }

    private void startTransmit(DataFrame dataRequest){
        // TODO: 16/3/25 这里没有reset dataRequest 的碰撞次数
        dataRequest.setStartTimeNow();
        setBusy();
        setCurrentDataRequest(dataRequest);
        Medium.getInstance().putRequest(dataRequest);
    }

    public void sendRequest(int targetId,long length){
        putRequest(new DataFrame(this.id,targetId,length));
    }

    private void setBusy(){
        this.isBusy = true;
    }

    private void setIdle(){
        this.isBusy = false;
    }

    private void setCurrentDataRequest(DataFrame dataRequest){
        this.currentDataRequest = dataRequest;
    }

    private void unSetDataRequest(){
        this.currentDataRequest = null;
    }

}
