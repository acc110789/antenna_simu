package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.clock.ClockController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.infrastructure.medium.MediumObserver;
import me.zhangxl.antenna.infrastructure.medium.MediumObservers;
import me.zhangxl.antenna.util.Pair;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 该类代表一个站点的状态
 * Created by zhangxiaolong on 16/3/24.
 */
public class Station implements MediumObserver {

    private static final Logger logger = new Logger(Station.class);
    private final int id;
    private final Pair<Double, Double> mLocation; //定向天线时需要保证
    //wait list
    private List<DataFrame> mDataFramesToSend = Collections.synchronizedList(new ArrayList<DataFrame>());
    private List<DataFrame> mDataFrameReceived = Collections.synchronizedList(new ArrayList<DataFrame>());
    private DataFrame mCurrentSendingFrame;

    static {
        Medium.getInstance();
    }

    public Station(int id) {
        this.id = id;
        mLocation = null;
        StationUtil.stationList.add(this);
        MediumObservers.getInstance().register(this);
    }

    public Station(int id, Double xAxis, Double yAxis) {
        this.id = id;
        this.mLocation = new Pair<>(xAxis, yAxis);
        StationUtil.stationList.add(this);
        MediumObservers.getInstance().register(this);
    }

    @Override
    public void onRtsCollision(List<RtsFrame> collisionFrames) {
        int count = 0;
        for (RtsFrame frame : collisionFrames) {
            if (frame.getSrcId() == this.id) {
                count++;
                mCurrentSendingFrame.addCollitionTimes();
            }
        }
        if (count > 1) {
            throw new IllegalStateException("too many frames sent by station id:" + this.id);
        }
    }

    @Override
    public void onNewSLot() {
        if (mCurrentSendingFrame != null) {
            mCurrentSendingFrame.countDownBackOff();
        } else {
            //mCurrentSendingFrame == null
            getDataFrameToSend();
        }
        sendDataIfNeed();
    }

    @Override
    public void onPostDifs() {
        if (mCurrentSendingFrame == null) {
            getDataFrameToSend();
        } else if (mCurrentSendingFrame.isCollision()) {
            mCurrentSendingFrame.unsetCollision();
        }
        sendDataIfNeed();
    }

    private void sendDataIfNeed() {
        if (mCurrentSendingFrame != null && mCurrentSendingFrame.canBeSent()) {
            //开始进入流程
            if (Logger.DEBUG_STATION) {
                logger.log("%d start transmit data frame:%d", this.getId(), mCurrentSendingFrame.getSerialNum());
            }
            sendRts(mCurrentSendingFrame.generateRtsFrame());
        }
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


    /**
     * 提供给
     * {@link me.zhangxl.antenna.application.App}
     * 调用
     */
    public void sendRequest(int targetId, long length) {
        mDataFramesToSend.add(new DataFrame(this.id, targetId, length));
    }

    public void sendRequest(int targetId, long length, int dataFrameId) {
        mDataFramesToSend.add(new DataFrame(this.id, targetId, length, dataFrameId));
    }

    int getWaitingRequestNum() {
        return mDataFramesToSend.size();
    }

    int getId() {
        return this.id;
    }

    //作为发送端发送的数据
    private void sendRts(RtsFrame frame) {
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    sendRts...");
        }
        Medium.getInstance().putRts(frame);
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

    //作为发送端的接受数据
    private void receiveCts(CtsFrame frame) {
        //需要等待一个SIFS之后再 sendData
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    receiveCts...");
        }
        ClockController.getInstance().post(new Runnable() {
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

    //作为接收端接受的数据
    private void receiveRts(final RtsFrame frame) {
        //需要等待一个SIFS回传一个Cts
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    receiveRts...");
        }
        ClockController.getInstance().post(new Runnable() {
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
        ClockController.getInstance().addDataAmount(frame.getLength() / 8);
        mDataFrameReceived.add(frame);
        ClockController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                sendAck(frame.generateAckFrame());
            }
        }, Config.getInstance().getSifs());
    }

    public void receiveFrame(Frame frame) {
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

}
