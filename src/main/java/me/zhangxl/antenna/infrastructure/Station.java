package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.infrastructure.clock.Statistic;
import me.zhangxl.antenna.infrastructure.cool.EifsCool;
import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.frame_process.ProcessorHelper;
import me.zhangxl.antenna.infrastructure.frame_process.SendRtsProcessor;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.*;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类代表一个站点,以及其行为和状态.
 * Created by zhangxiaolong on 16/3/24.
 */
public class Station extends AbstractRole{

    private static final Logger logger = SimuLoggerManager.getLogger(Station.class.getSimpleName());
    private Pair<Double, Double> mLocation; //定向天线时需要保证
    private DataFrame mCurrentSendingFrame;
    //wait list
    private List<DataFrame> mDataFramesToSend = new ArrayList<>();
    private double mLastCoolTime = 0;
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
        StationUtil.stationList.add(this);
        Medium.getInstance().register(this);
    }

    public Station(int id, double xAxis, double yAxis) {
        this(id);
        this.mLocation = new Pair<>(xAxis, yAxis);
    }

    public Pair<Double,Double> getAxis(){
        return this.mLocation;
    }

    public int getWaitingRequestNum() {
        return mDataFramesToSend.size();
    }

    public void onPostDIFS() {
        logger.debug("%d onPostDIFS", getId());
        assert getCurrentStatus() == Status.SLOTING;
        if (mCurrentSendingFrame == null) {
            getDataFrameToSend();
        } else {
            logger.info("%d current window: %d",getId(),mCurrentSendingFrame.getBackOff());
        }
        if(!sendDataIfNeed()){
            scheduleSLOT();
        }
    }

    private void onPostSLOT() {
        logger.debug("%d onPostSLOT", getId());
        assert getCurrentStatus() == Status.SLOTING;
        assert mCurrentSendingFrame != null;
        mCurrentSendingFrame.countDownBackOff();
        logger.info("%d current window: %d",getId(),mCurrentSendingFrame.getBackOff());
        if(!sendDataIfNeed()){
            scheduleSLOT();
        }
    }

    private void scheduleSLOT() {
        assert getCurrentStatus() == Status.SLOTING;
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(getCurrentStatus() == Status.SLOTING) {
                    //有可能Station已经作为接收端开始在接受信息了
                    //这种情况下,不能再执行onPostSLOT()了
                    onPostSLOT();
                }
            }
        }, Config.getInstance().getSlotLength());
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
            logger.debug("%d has no frame to send",getId());
        }
    }

    /**
     * @return 如果确实已经开始发送rts则返回true,如没有开始发送过程返回false
     */
    private boolean sendDataIfNeed() {
        if (mCurrentSendingFrame != null && mCurrentSendingFrame.canBeSent()) {
            //开始进入流程
            if (TimeLogger.DEBUG_STATION) {
                logger.debug("%d start transmit data frame sendDataIfNeed", this.getId());
            }
            mCurrentSendingFrame.setStartTimeNow();
            new SendRtsProcessor(this).processInner(mCurrentSendingFrame.generateRtsFrame());
            return true;
        }
        return false;
    }

    public void putDataFrame(int targetId, long length) {
        mDataFramesToSend.add(new DataFrame(getId(), targetId));
    }

    /**
     * 作为发送端发送成功时,加入一些统计数据
     */
    public void onSuccess() {
        logger.info("%d send a data successfully",getId());
        Statistic.addSuccessTimes();
        Statistic.addDataAmount(mCurrentSendingFrame.getLength() / 8);
        mDataFrameSent.add(mCurrentSendingFrame);
        mCurrentSendingFrame = null;
    }

    /**
     * 遭受到了碰撞
     */
    public void onFail() {
        Statistic.addCollitionTimes();
        mCurrentSendingFrame.addCollitionTimes();
    }

    @Override
    void assertNoReceivingFrameOnWriteMode() {
        assert receivingFrames.isEmpty();
    }

    /**
     * @param frame 开始接受frame一个新的,如果有正在接受的frame,
     *              则表明所有的frame发生了碰撞.则将所有的frame
     *              都标记为碰撞.注意,碰撞是指
     * @return accepted by this station
     */
    public boolean beginReceiveFrame(final Frame frame){
        //当station处于写数据模式  或者 处于NAV中时,不接受数据
        if(getCurrentStatus() == Status.NAV || !getCurrentStatus().isReadMode()){
            return false;
        }
        for(Frame frame1 : receivingFrames){
            if(StationUtil.hasIntersection(frame1,frame)){
                frame.setDirty();
                frame1.setDirty();
            }
        }
        receivingFrames.add(frame);
        if(getCurrentStatus() == Status.COOLING || getCurrentStatus() == Status.SLOTING){
            setCurrentStatus(Status.RECEIVING_RTS);
        } else if(getCurrentStatus() == Status.WAITING_CTS){
            setCurrentStatus(Status.RECEIVING_CTS);
        } else if(getCurrentStatus() == Status.WAITING_DATA){
            setCurrentStatus(Status.RECEIVING_DATA);
        } else if(getCurrentStatus() == Status.WAITING_ACK){
            setCurrentStatus(Status.RECEIVING_ACK);
        } else if(getCurrentStatus() != Status.RECEIVING_RTS &&
                getCurrentStatus() != Status.RECEIVING_CTS &&
                getCurrentStatus() != Status.RECEIVING_DATA &&
                getCurrentStatus() != Status.RECEIVING_ACK){
            throw new IllegalStateException();
        }
        int priority = TimeTask.COMMON_PRIORITY;
        if(frame instanceof CtsFrame){
            priority = TimeTask.POST_SEND_CTS;
        } else if(frame instanceof DataFrame){
            priority = TimeTask.POST_SEND_DATA;
        } else if(frame instanceof AckFrame){
            priority = TimeTask.POST_SEND_ACK;
        }
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                receivingFrames.remove(frame);
                if(!frame.isDirty()) {
                    //接收成功一个干净的桢
                    ProcessorHelper.process(Station.this,frame);
                } else if(receivingFrames.isEmpty()) {
                    //如果是脏的桢且是最后一个脏的桢,则在成功接受之后进入eifs信道冷却
                    new EifsCool(Station.this).cool();
                }
            }
        },frame.getEndDuration(),priority);
        return true;
    }

    @Override
    public DataFrame getDataToSend() {
        return mCurrentSendingFrame;
    }

    public void setLastCoolTimeNow(){
        this.mLastCoolTime = TimeController.getInstance().getCurrentTime();
    }

    public double getLastCoolTime(){
        return this.mLastCoolTime;
    }


}
