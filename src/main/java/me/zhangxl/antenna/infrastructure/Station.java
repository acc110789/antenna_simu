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
public class Station extends AbstractRole{

    private static final Logger logger = new Logger(Station.class);
    private final Sender mSender;
    private final Receiver mReceiver;
    private Pair<Double, Double> mLocation; //定向天线时需要保证

    private DataFrame mCurrentSendingFrame;
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
        this.mSender = new Sender(this);
        this.mReceiver = new Receiver(this);
        StationUtil.stationList.add(this);
        Medium.getInstance().register(this);
    }

    public Station(int id, Double xAxis, Double yAxis) {
        this(id);
        this.mLocation = new Pair<>(xAxis, yAxis);
    }

    int getWaitingRequestNum() {
        return mDataFramesToSend.size();
    }

    /**
     * 遭受到了碰撞
     */
    @Override
    void backOffDueToTimeout() {
        TimeController.getInstance().addCollitionTimes();
        mCurrentSendingFrame.addCollitionTimes();
        mCurrentSendingFrame.setStartTimeNow();
    }
    
    @Override
    public void scheduleDIFS(boolean Immediate) {
        assert getCurrentStatus() == Status.IDLE;
        if(Immediate){
            onPostDIFS();
        } else {
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    if(getCurrentStatus() == Status.IDLE) {
                        //如果过了DIFS状态仍然是IDLE,则证明可以postDIFS
                        // TODO: 16/4/15  一个Station有可能在一个DIFS期间从IDLE变成非IDLE,然后又变回IDLE
                        onPostDIFS();
                    }
                }
            }, Config.getInstance().getDifs());
        }
    }

    private void onPostDIFS() {
        assert !inNAV();
        assert getCurrentStatus() == Status.IDLE;
        if (mCurrentSendingFrame == null) {
            getDataFrameToSend();
        } else if (mCurrentSendingFrame.isCollision()) {
            mCurrentSendingFrame.unsetCollision();
        }
        sendDataIfNeed();
        scheduleSlotIfNeed();
    }

    private void scheduleSLOT() {
        assert !inNAV();
        assert getCurrentStatus() == Status.IDLE;
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(getCurrentStatus() == Status.IDLE) {
                    //有可能Station已经作为接收端开始在接受信息了
                    //这种情况下,不能再执行onPostSLOT()了
                    onPostSLOT();
                }
            }
        }, Config.getInstance().getSlotLength());
    }

    private void scheduleSlotIfNeed(){
        if(getCurrentStatus() == Status.IDLE){
            scheduleSLOT();
        }
    }

    private void onPostSLOT() {
        assert getCurrentStatus() == Status.IDLE;
        logger.log("%d onPostSLOT", getId());
        if (mCurrentSendingFrame != null) {
            mCurrentSendingFrame.countDownBackOff();
        } else {
            //mCurrentSendingFrame == null
            getDataFrameToSend();
        }
        sendDataIfNeed();
        scheduleSlotIfNeed();
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
            mSender.onPreSendRTS(mCurrentSendingFrame.generateRtsFrame());
            TimeController.getInstance().addSendTimes();
        }
    }

    void putDataFrame(int targetId, long length) {
        mDataFramesToSend.add(new DataFrame(getId(), targetId));
    }

    public void putDataFrame(int targetId, long length, int dataFrameId) {
        mDataFramesToSend.add(new DataFrame(getId(), targetId, dataFrameId));
    }

    @Override
    public void onSendSuccess() {
        TimeController.getInstance().addDataAmount(mCurrentSendingFrame.getLength() / 8);
        mDataFrameSent.add(mCurrentSendingFrame);
        mCurrentSendingFrame = null;
    }


    /**
     * 前提是碰撞发生了,找到最晚的那个frame,并且在在最晚的那个frame结束之后,安排DIFS
     */
    private void findLatestCollisionFrame(){
        double latestTime = -1;
        Frame latestFrame = null;
        for(Frame frame1 : receivingFrames){
            if(frame1.getEndTime() > latestTime){
                latestTime = frame1.getEndTime();
                latestFrame = frame1;
            }
        }
        assert latestFrame != null;

        scheduleLatestCollisionFrame(latestFrame);
    }

    private void scheduleLatestCollisionFrame(final Frame latestFrame){
        double timeToDo = latestFrame.getEndTime()-TimeController.getInstance().getCurrentTime();
        if(timeToDo <= 0){
            throw new IllegalArgumentException("remain time is less than 0");
        }
        //防止多次被schedule
        if(!latestFrame.scheduled()) {
            latestFrame.setScheduled();
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    boolean needSchedule = false;
                    if (receivingFrames.size() == 0) {
                        needSchedule = true;
                    } else if (receivingFrames.size() == 1) {
                        needSchedule = receivingFrames.get(0) == latestFrame;
                    }
                    if (needSchedule) {
                        logger.log("%d collision",getId());
                        onPostCommunication(false, false);
                    }
                }
            }, timeToDo);
        }
        for(Frame frame : receivingFrames){
            frame.setScheduled();
        }
    }

    /**
     * @param frame 开始接受frame一个新的,如果有正在接受的frame,
     *              则表明所有的frame发生了碰撞.则将所有的frame
     *              都标记为碰撞.
     * @return accepted by this station
     */
    public boolean beginReceiveFrame(final Frame frame){
        //当station不是读数据模式  或者 处于NAV中时,不接受数据
        if(getCurrentMode() != Mode.READ_MODE || inNAV()){
            return false;
        }
        //frame的起始传输时刻是当前时候,只要receivingFrames中存在一个Frame的
        //终点传输时刻大于当前时刻,则表明frame会遭到碰撞
        //如果终点传输时刻等于当前时刻,本仿真试验认为不会发生碰撞
        for(Frame frame1 : receivingFrames){
            if(frame.getStartTime() < frame1.getEndTime()){
                frame.setCollision();
                frame1.setCollision();
            } else if(frame.getStartTime() > frame1.getEndTime()){
                throw new IllegalStateException(getId() + " has rubbish");
            }
        }
        receivingFrames.add(frame);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                receivingFrames.remove(frame);
            }
        },frame.getTransmitDuration());

        if(frame.collision()){
            findLatestCollisionFrame();
            return true;
        }

        if (frame instanceof RtsFrame) {
            mReceiver.onPreRecvRTS((RtsFrame) frame);
        } else if (frame instanceof CtsFrame) {
            mSender.onPreRecvCTS((CtsFrame) frame);
        } else if (frame instanceof DataFrame) {
            mReceiver.onPreRecvData((DataFrame) frame);
        } else if (frame instanceof AckFrame) {
            mSender.onPreRecvACK((AckFrame) frame);
        } else {
            throw new IllegalArgumentException("unspecified frame type " + frame.getClass().getSimpleName());
        }
        return true;
    }

    @Override
    public DataFrame getDataToSend() {
        return mCurrentSendingFrame;
    }


}
