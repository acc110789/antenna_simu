package me.zhangxl.antenna.infrastructure;

import com.sun.tools.javac.util.Pair;
import me.zhangxl.antenna.request.*;
import me.zhangxl.antenna.util.Config;
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
    public void onRtsCollision(List<RtsFrame> collisionFrames) {
        int count = 0; //
        for(RtsFrame frame : collisionFrames){
            if(frame.getSrcId() == this.id){
                count ++;
                mCurrentSendingFrame.addCollitionTimes();
            }
        }
        if(count > 1){
            throw new IllegalStateException("too many frames sent by station id:"+this.id);
        }
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
        } else if(mCurrentSendingFrame.isConflicting()){
            mCurrentSendingFrame.unsetConflict();
        }
        sendDataIfNeed();
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
    private void sendRts(RtsFrame frame){
        Medium.getInstance().putFrame(frame);
    }

    private void sendData(){
        if(mCurrentSendingFrame == null){
            throw new IllegalStateException("current Frame of Station is null");
        }
        Medium.getInstance().putFrame(mCurrentSendingFrame);
    }

    //作为发送端的接受数据
    private void receiveCts(CtsFrame frame){
        //需要等待一个SIFS之后再 sendData
        ClockController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                sendData();
            }
        },Config.SIFS);
    }

    private void receiveAck(AckFrame frame){
        ClockController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                Medium.getInstance().setFree();
            }
        },Config.DIFS);
    }

    //作为接受端发送的数据
    private void sendCts(CtsFrame frame){
        Medium.getInstance().putFrame(frame);
    }

    private void sendAck(AckFrame frame){
        Medium.getInstance().putFrame(frame);
    }

    //作为接收端接受的数据
    private void receiveRts(final RtsFrame frame){
        //需要等待一个SIFS回传一个Cts
        ClockController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                sendCts(frame.generateCtsFrame());
            }
        },Config.SIFS);
    }

    private void receiveData(final DataFrame frame){
        //等待一个SIFS之后 回复一个AckFrame
        mDataFrameReceived.add(frame);
        ClockController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                sendAck(frame.generateAckFrame());
            }
        },Config.SIFS);
    }

    void receiveFrame(Frame frame){
        //如果frame的目标地址不是自己,则丢弃这个frame
        if(frame.getTargetId() != this.id){
            return;
        }

        if(frame instanceof RtsFrame){
            receiveRts((RtsFrame) frame);
        } else if(frame instanceof CtsFrame){
            receiveCts((CtsFrame) frame);
        } else if(frame instanceof DataFrame){
            receiveData((DataFrame) frame);
        } else if(frame instanceof AckFrame){
            receiveAck((AckFrame) frame);
        } else {
            throw new IllegalArgumentException("unspecified frame type");
        }
    }

}
