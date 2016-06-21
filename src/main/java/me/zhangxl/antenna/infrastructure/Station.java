package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.clock.Statistic;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.frame_process.SendRtsProcessor;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.infrastructure.station.SlotManager;
import me.zhangxl.antenna.infrastructure.station.StationFreFilter;
import me.zhangxl.antenna.infrastructure.station.receive_logic.OnReceiveAckFrame;
import me.zhangxl.antenna.infrastructure.station.receive_logic.OnReceiveDataFrame;
import me.zhangxl.antenna.infrastructure.station.receive_logic.OnReceiveNextRoundFrame;
import me.zhangxl.antenna.infrastructure.station.receive_logic.OnReceivePairFrame;
import me.zhangxl.antenna.util.Pair;
import me.zhangxl.antenna.util.SimuLoggerManager;
import me.zhangxl.antenna.util.StationUtil;
import me.zhangxl.antenna.util.TimeLogger;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类代表一个站点,以及其行为和状态.
 * Created by zhangxiaolong on 16/3/24.
 */
public class Station extends AbstractRole{

    private static final Logger logger = SimuLoggerManager.getLogger(Station.class.getSimpleName());
    //当前要发送的DataFrame
    private DataFrame mCurrentSendingFrame;
    //当进行difs或者eifs的时候需要记录开始冷却的时间
    private double mLastCoolTime = 0;
    //等待被发送的frames
    private List<DataFrame> mDataFramesToSend = new ArrayList<>();
    //已经发送成功的frames
    private List<DataFrame> mDataFrameSent = new ArrayList<>();
    //当前正在接受的frames
    private List<Frame> receivingFrames = new ArrayList<>();
    //频率过滤器
    private StationFreFilter mFreFilter = new StationFreFilter();
    private final SlotManager mSlotManager = new SlotManager(this);
    private double lastCoolingTime = -1;

    public Station(int id) {
        super(id);
        registerInfo();
    }

    public Station(int id, double xAxis, double yAxis) {
        super(id,xAxis,yAxis);
        registerInfo();
    }

    public Station(int id, Pair<Double,Double> location){
        super(id,location);
        registerInfo();
    }

    private void registerInfo(){
        StationUtil.stationList.add(this);
        Medium.getInstance().register(this);
    }

    public int getWaitingRequestNum() {
        return mDataFramesToSend.size();
    }

    public void putDataFrame(int targetId) {
        mDataFramesToSend.add(new DataFrame(getId(), targetId,-1));
    }

    /**
     * 如果存在待发送的Frame,则取出一个
     * Frame,设置为当前要发送的Frame,
     * 并为这个Frame进行初始化
     */
    void setNextDataFrameToSend() {
        assert mCurrentSendingFrame == null;
        if (mDataFramesToSend.size() > 0) {
            mCurrentSendingFrame = mDataFramesToSend.remove(0);
            StationUtil.guaranteeEnoughFrame(this);
            mCurrentSendingFrame.init();
        } else {
            logger.debug("%d has no frame to send",getId());
        }
    }

    /**
     * 作为发送端发送成功时,加入一些统计数据
     */
    public void onSuccess() {
        logger.info("%d send a data successfully",getId());
        Statistic.addSuccessTimes();
        Statistic.addDataAmount(mCurrentSendingFrame.getLength());
        Statistic.addTranmitCount(mCurrentSendingFrame.getFailTimes() + 1);
        mDataFrameSent.add(mCurrentSendingFrame);
        mCurrentSendingFrame = null;
    }

    /**
     * 做为发送端,遭受到了碰撞,将backoff的窗口增加一倍
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
        //如果频率不是这个station care的频率,就跟这个频率好像不存在是一样的
        if(getCurrentStatus() == Status.NAV || !mFreFilter.canReceive(frame.getFre())){
            return true;
        }
        if(getCurrentStatus().isWriteMode()){
            throw new IllegalStateException("不可能是WriteMode");
        }
        if(receivingFrames.size() > 0){
            //在同时接受的frame应该是只有一个
            throw new IllegalStateException("应该不会有正在接受的frame");
        }
        receivingFrames.add(frame);

        if(frame instanceof NextRoundFrame){
            new OnReceiveNextRoundFrame(this,frame).doLogic();
        } else if(frame instanceof PairFrame){
            new OnReceivePairFrame(this,frame).doLogic();
        } else if(frame instanceof DataFrame){
            new OnReceiveDataFrame(this,frame).doLogic();
        } else if(frame instanceof  AckFrame){
            new OnReceiveAckFrame(this,frame).doLogic();
        }
        return true;
    }

    public DataFrame getDataFrameToSend() {
        return mCurrentSendingFrame;
    }

    public void setLastCoolTimeNow(){
        this.mLastCoolTime = TimeController.getInstance().getCurrentTime();
    }

    public double getLastCoolTime(){
        return this.mLastCoolTime;
    }


    public List<Frame> getReceivingFrames() {
        return receivingFrames;
    }

    public void setAcceptFre(int channel) {
        mFreFilter.setFre(channel);
    }

    public void onNextRound(int slots) {
        setCurrentStatus(Status.SLOTING);
        mSlotManager.setAvailableSlotCount(slots);
        if(mCurrentSendingFrame == null){
            //说明上次的发送成功,mCurrentSendingFrame被放在了已发送list里面了
            setNextDataFrameToSend();
        } else if(mCurrentSendingFrame.canBeSent()){
            //说明上次曾经尝试发送,但是PCP节点没有给机会,这样相当于碰撞,应该将窗口加倍
            // TODO: 16/6/21 是这样吗?有待商榷
            onFail();
        }
        if (!sendDataIfNeed()) {
            mSlotManager.scheduleSLOT();
        }
    }

    /**
     * @return 当确实开始发送一个RTSFrame时返回true,
     * 如果slot还没有减少到0,则代表没有开始发送一个RtsFrame
     * 这种情况下返回false.
     */
    public boolean sendDataIfNeed() {
        if (mCurrentSendingFrame != null && mCurrentSendingFrame.canBeSent()) {
            //开始进入流程
            if (TimeLogger.DEBUG_STATION) {
                logger.debug("%d start transmit data frame sendDataIfNeed", this.getId());
            }
            mCurrentSendingFrame.setStartTimeNow();
            new SendRtsProcessor(this).process(FrameHelper.generateRtsFrame(mCurrentSendingFrame));
            return true;
        }
        return false;
    }

    public void setCoolTimeNow(){
        this.lastCoolingTime = TimeController.getInstance().getCurrentTime();
    }

    public double getLastCoolingTime(){
        return this.lastCoolingTime;
    }
}
