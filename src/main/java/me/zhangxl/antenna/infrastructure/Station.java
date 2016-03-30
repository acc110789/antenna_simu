package me.zhangxl.antenna.infrastructure;

import com.sun.tools.javac.util.Pair;
import me.zhangxl.antenna.request.DataFrame;
import me.zhangxl.antenna.util.Logger;

import java.util.*;

/**
 * 该类代表一个站点的状态
 * Created by zhangxiaolong on 16/3/24.
 */
public class Station implements MediumObserver {

    private static final Logger logger = new Logger(Station.class);
    //wait list
    private List<DataFrame> mDataFramesToSend = Collections.synchronizedList(new ArrayList<DataFrame>());

    private List<DataFrame> mDataFrameReceived = Collections.synchronizedList(new ArrayList<DataFrame>());

    private final int id;

    private final Pair<Double,Double> mLocation; //定向天线时需要保证

    public Station(int id){
        this.id = id;
        mLocation = null;
        MediumObservers.getInstance().register(this);
    }

    public Station(int id,Double xAxis,Double yAxis){
        this.id = id;
        this.mLocation = new Pair<>(xAxis,yAxis);
    }

    @Override
    public void onRtsCollision() {

    }

    @Override
    public void onNewSLot() {

    }

    private void startTransmit(DataFrame dataRequest){
        // TODO: 16/3/25 这里没有reset dataRequest 的碰撞次数
        dataRequest.setStartTimeNow();
        Medium.getInstance().putRequest(dataRequest);
    }

    /**提供给 {@link me.zhangxl.antenna.application.App} 调用*/
    public void sendRequest(int targetId,long length){
        mDataFramesToSend.add(new DataFrame(this.id,targetId,length));
    }

    //作为发送端发送的数据
    public void sendRts(){

    }

    public void sendData(){

    }

    //作为发送端的接受数据
    public void receiveCts(){

    }

    public void receiveAck(){

    }

    //作为接受端发送的数据
    public void sendCts(){

    }

    public void sendAck(){

    }

    //作为接收端接受的数据
    public void receiveRts(){

    }

    public void receiveData(){

    }

}
