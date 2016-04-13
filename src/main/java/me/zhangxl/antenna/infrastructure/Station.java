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

    private final Pair<Double, Double> mLocation; //定向天线时需要保证
    //wait list
    private List<DataFrame> mDataFramesToSend = new ArrayList<>();
    /**
     * 已经发送成功的frames
     */
    private List<DataFrame> mDataFrameSent = new ArrayList<>();
    /**
     * 正在接受的frames
     */
    private List<Frame> receivingFrames = new ArrayList<>();

    public Station(int id) {
        super(id);
        mLocation = null;
        StationUtil.stationList.add(this);
        Medium.getInstance().register(this);
    }

    public Station(int id, Double xAxis, Double yAxis) {
        super(id);
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

    public void backOffDueToTimeout() {
        mCurrentSendingFrame.addCollitionTimes();
        mCurrentSendingFrame.setStartTimeNow();
        scheduleDIFS(true);
    }

    @Override
    public void scheduleDIFS(boolean Immediate) {
        assertCurrentStatus(Status.IDLE);
        if(Immediate){
            onPostDIFS();
        } else {
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    if(currentStatus == Status.IDLE) {
                        //如果过了DIFS状态仍然是IDLE,则证明可以postDIFS
                        // TODO: 16/4/13  这里面可能存在一个bug,即Status从IDLE变成非IDLE,然后又变回IDLE
                        onPostDIFS();
                    }
                }
            }, Config.getInstance().getDifs());
        }
    }

    private void scheduleSlotIfNeed(){
        if(currentStatus == Status.IDLE){
            scheduleSLOT();
        }
    }

    @Override
    void onPostDIFS() {
        if (mCurrentSendingFrame == null) {
            getDataFrameToSend();
        } else if (mCurrentSendingFrame.isCollision()) {
            mCurrentSendingFrame.unsetCollision();
        }
        sendDataIfNeed();
        scheduleSlotIfNeed();
    }

    @Override
    public void scheduleSLOT() {
        assertCurrentStatus(Status.IDLE);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSLOT();
            }
        }, Config.getInstance().getSlotLength());
    }

    @Override
    public void onPostSLOT() {
        //有可能Station已经作为接收端开始在接受信息了
        //这种情况下,不能再执行onPostSLOT()了
        if(currentStatus == Status.IDLE) {
            logger.log("station :%d onPostSLOT", this.id);
            if (mCurrentSendingFrame != null) {
                mCurrentSendingFrame.countDownBackOff();
            } else {
                //mCurrentSendingFrame == null
                getDataFrameToSend();
            }
            sendDataIfNeed();
            scheduleSlotIfNeed();
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

    private void sendDataIfNeed() {
        if (mCurrentSendingFrame != null && mCurrentSendingFrame.canBeSent()) {
            //开始进入流程
            if (Logger.DEBUG_STATION) {
                logger.log("%d start transmit data frame sendDataIfNeed", this.getId());
            }
            onPreSendRTS(mCurrentSendingFrame.generateRtsFrame());
        }
    }

    public void putDataFrame(int targetId, long length) {
        mDataFramesToSend.add(new DataFrame(this.id, targetId));
    }

    public void putDataFrame(int targetId, long length, int dataFrameId) {
        mDataFramesToSend.add(new DataFrame(this.id, targetId, dataFrameId));
    }

    //作为发送端发送的数据
    @Override
    protected void onPreSendRTS(RtsFrame frame) {
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    onPreSendRTS...");
        }
        super.onPreSendRTS(frame);
        frame.setStartTimeNow();
        Medium.getInstance().putFrame(this,frame);
    }

    @Override
    void onPreSendCTS(CtsFrame frame) {
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    onPreSendCTS...");
        }
        super.onPreSendCTS(frame);
        frame.setStartTimeNow();
        Medium.getInstance().putFrame(this,frame);
    }

    @Override
    void onPreSendData(DataFrame frame) {
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    onPreSendDATA...");
        }
        super.onPreSendData(frame);
        frame.setStartTimeNow();
        Medium.getInstance().putFrame(this,frame);
    }

    @Override
    void onPreSendAck(AckFrame frame) {
        if (Logger.DEBUG_STATION) {
            logger.log(this.id + "    onPreSendAck...");
        }
        super.onPreSendAck(frame);
        frame.setStartTimeNow();
        Medium.getInstance().putFrame(this,frame);
    }

    @Override
    void onPostRecvACK(AckFrame frame) {
        if(!frame.collision()) {
            logger.log("%d onPostRecvACK()",id);
            if (Logger.DEBUG_STATION) {
                logger.log(this.id + "    send a data successfully...");
            }
            TimeController.getInstance().addDataAmount(mCurrentSendingFrame.getLength() / 8);
            mDataFrameSent.add(mCurrentSendingFrame);
            mCurrentSendingFrame = null;
        }
        //不管是不是碰撞都要释放状态
        currentStatus = Status.IDLE;
        resetCommunicationTarget();
    }

    /**
     * 前提是碰撞发生了,找到最晚的那个frame,并且在在最晚的那个frame结束之后,安排DIFS
     */
    private void findLatestFrameAndScheduleDIFS(){
        double latestTime = -1;
        Frame latestFrame = null;
        for(Frame frame1 : receivingFrames){
            if(frame1.getEndTime() > latestTime){
                latestTime = frame1.getEndTime();
                latestFrame = frame1;
            }
        }
        assert latestFrame != null;

        double timeToDo = latestFrame.getEndTime()-TimeController.getInstance().getCurrentTime();
        if(timeToDo <= 0){
            throw new IllegalArgumentException("remain time is less than 0");
        }
        final Frame finalLatestFrame = latestFrame;

        //防止多次被schedule
        if(!finalLatestFrame.scheduled()) {
            finalLatestFrame.setScheduled();
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    boolean needSchedule = false;
                    if (receivingFrames.size() == 0) {
                        needSchedule = true;
                    } else if (receivingFrames.size() == 1) {
                        needSchedule = receivingFrames.get(0) == finalLatestFrame;
                    }
                    if (needSchedule) {
                        currentStatus = Status.IDLE;
                        resetCommunicationTarget();
                        scheduleDIFS(false);
                    }
                }
            }, timeToDo);
        }
    }

    // TODO: 16/4/8 A被B发送ACK,刚好发送完成,这时候C给B发送RTS,这个情况的ACK和RTS算不算碰撞
    /**
     * @param frame 开始接受frame一个新的,如果有正在接受的frame,
     *              则表明所有的frame发生了碰撞.则将所有的frame
     *              都标记为碰撞.
     * @return accepted by this station
     */
    public boolean beginReceiveFrame(final Frame frame){
        //必须处于监听模式才能读取数据
        if(currentMode != READ_MODE){
            return false;
        }
        receivingFrames.add(frame);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                receivingFrames.remove(frame);
            }
        },frame.getTransmitDuration());

        if(receivingFrames.size() > 1){
            for(Frame frame1 : receivingFrames){
                frame1.setCollision();
            }
            findLatestFrameAndScheduleDIFS();
            return true;
        }

        if (frame instanceof RtsFrame) {
            onPreRecvRTS((RtsFrame) frame);
        } else if (frame instanceof CtsFrame) {
            onPreRecvCTS((CtsFrame) frame);
        } else if (frame instanceof DataFrame) {
            onPreRecvData((DataFrame) frame);
        } else if (frame instanceof AckFrame) {
            onPreRecvACK((AckFrame) frame);
        } else {
            throw new IllegalArgumentException("unspecified frame type " + frame.getClass().getSimpleName());
        }
        return true;
    }

}
