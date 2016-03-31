package me.zhangxl.antenna.infrastructure;

import com.sun.tools.javac.util.Pair;
import me.zhangxl.antenna.request.*;
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

    private DataFrame mCurrentSendingFrame;

    private Frame mCurrentReceivingFrame;

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
        if(mCurrentSendingFrame != null){
            mCurrentSendingFrame.countDownBackOff();
            sendDataIfNeed();
        }
    }

    @Override
    public void onPostDifs() {
        if(mCurrentSendingFrame == null){
            getDataFrameToSend();
            sendDataIfNeed();
        }
    }

    private void sendDataIfNeed(){
        if(mCurrentSendingFrame != null && mCurrentSendingFrame.canBeSent()){
            //开始进入流程
            sendRts(mCurrentSendingFrame.generateRtsFrame());
        }
    }

    /**
     * 如果存在待发送的Frame,则取出一个
     * Frame,设置为当前要发送的Frame,
     * 并为这个Frame进行初始化
     */
    private void getDataFrameToSend(){
        if(mDataFramesToSend.size() > 0) {
            mCurrentSendingFrame = mDataFramesToSend.remove(0);
            mCurrentSendingFrame.init();
        }
    }


    /**提供给
     * {@link me.zhangxl.antenna.application.App}
     * 调用*/
    public void sendRequest(int targetId,long length){
        mDataFramesToSend.add(new DataFrame(this.id,targetId,length));
    }

    //作为发送端发送的数据
    public void sendRts(RtsFrame frame){
        Medium.getInstance().putRequest(frame);
    }

    public void sendData(){
        if(mCurrentSendingFrame == null){
            throw new IllegalStateException("current Frame of Station is null");
        }
        Medium.getInstance().putRequest(mCurrentSendingFrame);
    }

    //作为发送端的接受数据
    public void receiveCts(CtsFrame frame){

    }

    public void receiveAck(AckFrame frame){

    }

    //作为接受端发送的数据
    public void sendCts(){

    }

    public void sendAck(){

    }

    //作为接收端接受的数据
    public void receiveRts(){
        //需要等待一个SIFS再进行
    }

    public void receiveData(){

    }

}
