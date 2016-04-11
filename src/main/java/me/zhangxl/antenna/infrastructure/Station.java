package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;
import me.zhangxl.antenna.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类代表一个站点,以及其行为和状态.
 * Created by zhangxiaolong on 16/3/24.
 */
public class Station extends Stateful {

    private static final Logger logger = new Logger(Station.class);
    private final int id;
    private final Pair<Double, Double> mLocation; //定向天线时需要保证
    //wait list
    private List<DataFrame> mDataFramesToSend = new ArrayList<>();
    private List<DataFrame> mDataFrameSent = new ArrayList<>();
    private List<Frame> receivingFrames = new ArrayList<>();
    private DataFrame mCurrentSendingFrame;
    /**
     * readMode 为true时代表Station此时处于读的模式
     * 为false时代表Station此时处于写的模式
     */
    private boolean readMode = true;

    public Station(int id) {
        this.id = id;
        mLocation = null;
        StationUtil.stationList.add(this);
        Medium.getInstance().register(this);
    }

    public Station(int id, Double xAxis, Double yAxis) {
        this.id = id;
        this.mLocation = new Pair<>(xAxis, yAxis);
        StationUtil.stationList.add(this);
        Medium.getInstance().register(this);
    }

    int getWaitingRequestNum() {
        return mDataFramesToSend.size();
    }

    int getId() {
        return this.id;
    }

    public void onCollision() {
        mCurrentSendingFrame.addCollitionTimes();
    }

    @Override
    public void onPostSLOT() {
        if (mCurrentSendingFrame != null) {
            mCurrentSendingFrame.countDownBackOff();
        } else {
            //mCurrentSendingFrame == null
            getDataFrameToSend();
        }
        sendDataIfNeed();
    }

    @Override
    public void onPostDIFS() {
        if (mCurrentSendingFrame == null) {
            getDataFrameToSend();
        } else if (mCurrentSendingFrame.isCollision()) {
            mCurrentSendingFrame.unsetCollision();
        }
        sendDataIfNeed();
    }

    /**
     * 如果存在待发送的Frame,则取出一个
     * Frame,设置为当前要发送的Frame,
     * 并为这个Frame进行初始化
     */
    private void getDataFrameToSend() {
        if (mDataFramesToSend.size() > 0) {
            mCurrentSendingFrame = mDataFramesToSend.remove(0);
            StationUtil.guaranteeEnoughFrame(this);
            mCurrentSendingFrame.init();
        } else {
            logger.log("%d has no frame to send",getId());
        }
    }

    private void sendDataIfNeed() {
        if (mCurrentSendingFrame != null && mCurrentSendingFrame.canBeSent()) {
            //开始进入流程
            if (Logger.DEBUG_STATION) {
                logger.log("%d start transmit data frame:%d", this.getId(), mCurrentSendingFrame.getSerialNum());
            }
            onPreSendRTS(mCurrentSendingFrame.generateRtsFrame());
        }
    }

    /**
     * 提供给
     * {@link me.zhangxl.antenna.application.App}
     * 调用
     */
    public void putDataFrame(int targetId, long length) {
        mDataFramesToSend.add(new DataFrame(this.id, targetId, length));
    }

    public void putDataFrame(int targetId, long length, int dataFrameId) {
        mDataFramesToSend.add(new DataFrame(this.id, targetId, length, dataFrameId));
    }

    //作为发送端发送的数据
    @Override
    protected void onPreSendRTS(RtsFrame frame) {
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    onPreSendRTS...");
        }
        super.onPreSendRTS(frame);
        Medium.getInstance().putRts(this,frame);
    }

    private void sendData() {
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    sendData...");
        }
        if (mCurrentSendingFrame == null) {
            throw new IllegalStateException("current Frame of Station is null");
        }
        Medium.getInstance().putFrame(mCurrentSendingFrame);
    }

    //作为接受端发送的数据
    private void sendCts(CtsFrame frame) {
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    sendCts...");
        }
        Medium.getInstance().putFrame(frame);
    }

    private void sendAck(AckFrame frame) {
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    sendAck...");
        }
        Medium.getInstance().putFrame(frame);
    }

    //作为发送端的接受数据
    private void receiveCts(CtsFrame frame) {
        //需要等待一个SIFS之后再 sendData
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    receiveCts...");
        }
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                sendData();
            }
        }, Config.getInstance().getSifs());
    }

    private void receiveAck(AckFrame frame) {
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    receiveAck...");
        }
        //已经发送完毕
        mCurrentSendingFrame = null;
    }


    //作为接收端接受的数据
    private void receiveRts(final RtsFrame frame) {
        //需要等待一个SIFS回传一个Cts
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    receiveRts...");
        }
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                sendCts(frame.generateCtsFrame());
            }
        }, Config.getInstance().getSifs());
    }

    private void receiveData(final DataFrame frame) {
        //等待一个SIFS之后 回复一个AckFrame
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    receiveData...");
        }
        TimeController.getInstance().addDataAmount(frame.getLength() / 8);
        mDataFrameReceived.add(frame);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                sendAck(frame.generateAckFrame());
            }
        }, Config.getInstance().getSifs());
    }

    private void receiveFrame(Frame frame) {
        //如果frame的目标地址不是自己,则丢弃这个frame.
        //碰撞的GarbageFrame的TargetId 和任何Station的Id都不相同
        if (frame.getTargetId() != this.id) {
            return;
        }

        if (frame instanceof RtsFrame) {
            receiveRts((RtsFrame) frame);
        } else if (frame instanceof CtsFrame) {
            receiveCts((CtsFrame) frame);
        } else if (frame instanceof DataFrame) {
            receiveData((DataFrame) frame);
        } else if (frame instanceof AckFrame) {
            receiveAck((AckFrame) frame);
        } else {
            throw new IllegalArgumentException("unspecified frame type " + frame.getClass().getSimpleName());
        }
    }

    // TODO: 16/4/8 A被B发送ACK,刚好发送完成,这时候C给B发送RTS,这个情况的ACK和RTS算不算碰撞
    /**
     * @param frame 开始接受frame一个新的,如果有正在接受的frame,
     *              则表明所有的frame发生了碰撞.则将所有的frame
     *              都标记为碰撞,对于碰撞的frame到时候不予处理
     */
    public void beginReceiveFrame(final Frame frame){
        receivingFrames.add(frame);
        if(receivingFrames.size() > 1){
            for(Frame frame1 : receivingFrames){
                frame1.setCollision();
            }
        }

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                receivingFrames.remove(frame);
                if(!frame.collision()){
                    receiveFrame(frame);
                }
            }
        },frame.getTransmitDuration());
    }

}
