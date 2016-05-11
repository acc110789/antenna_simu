package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.base.ChannelManager;
import me.zhangxl.antenna.infrastructure.base.Locatable;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.infrastructure.station.receive_logic.OnReceiveAckFrame;
import me.zhangxl.antenna.infrastructure.station.receive_logic.OnReceiveDataFrame;
import me.zhangxl.antenna.infrastructure.station.receive_logic.OnReceiveNextRoundFrame;
import me.zhangxl.antenna.infrastructure.station.receive_logic.OnReceivePairFrame;
import me.zhangxl.antenna.util.Pair;
import me.zhangxl.antenna.util.SimuLoggerManager;
import me.zhangxl.antenna.util.TimeLogger;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类代表一个站点,以及其行为和状态.
 * Created by zhangxiaolong on 16/3/24.
 */
public class Station extends AbstractRole implements Locatable {

    private static final Logger logger = SimuLoggerManager.getLogger(Station.class.getSimpleName());
    private final Sender mSender;
    private final Receiver mReceiver;
    private Pair<Double, Double> mLocation; //定向天线时需要保证
    private double lastCoolingTime;

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
    private final StationFreFilter mFreFilter = new StationFreFilter();
    private final SlotManager mSlotManager = new SlotManager(this);

    public Station(int id) {
        super(id);
        this.mSender = new Sender(this);
        this.mReceiver = new Receiver(this);
        StationUtil.stationList.add(this);
        Medium.getInstance().register(this);
        ChannelManager.getInstance();
    }

    public Station(int id, double xAxis, double yAxis) {
        this(id);
        this.mLocation = new Pair<>(xAxis, yAxis);
    }

    public Station(int id,Pair<Double,Double> axis){
        this(id);
        this.mLocation = axis;
    }

    public Pair<Double,Double> getAxis(){
        return this.mLocation;
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
     * @return 当确实开始发送一个RTSFrame时返回true,
     * 如果slot还没有减少到0,则代表没有开始发送一个RtsFrame
     * 这种情况下返回false.
     */
    boolean sendDataIfNeed() {
        if (mCurrentSendingFrame != null && mCurrentSendingFrame.canBeSent()) {
            //开始进入流程
            if (TimeLogger.DEBUG_STATION) {
                logger.debug("%d start transmit data frame sendDataIfNeed", this.getId());
            }
            mCurrentSendingFrame.setStartTimeNow();
            mSender.onPreSendRTS(mCurrentSendingFrame.generateRtsFrame());
            return true;
        }
        return false;
    }

    void putDataFrame(int targetId, long length,int fre) {
        mDataFramesToSend.add(new DataFrame(getId(), targetId,fre));
    }

    void onSendSuccess() {
        logger.info("%d send a data successfully",getId());
        TimeController.getInstance().addSuccessTimes();
        TimeController.getInstance().addDataAmount(mCurrentSendingFrame.getLength() / 8);
        mDataFrameSent.add(mCurrentSendingFrame);
        getDataFrameToSend();
    }

    /**
     * @param frame Station接受的桢从理论上说是不可能发生任何冲突的
     */
    public boolean beginReceiveFrame(Frame frame){
        //如果频率不是这个station care的频率,就跟这个频率好像不存在是一样的
        if(getCurrentStatus() == Status.NAVING || !mFreFilter.canReceive(frame.getFre())){
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

    public void onNextRound(int slots){
        setCurrentStatus(Status.SLOTING);
        mSlotManager.setAvailableSlotCount(slots);
        if(mCurrentSendingFrame == null){
            //说明上次的发送成功,mCurrentSendingFrame被放在了已发送list里面了
            getDataFrameToSend();
        } else if(mCurrentSendingFrame.canBeSent()){
            //说明上次曾经尝试发送,但是PCP节点没有给机会,这样相当于碰撞,应该将窗口加倍
            backOffDueToTimeout();
        }
        if (!sendDataIfNeed()) {
            mSlotManager.scheduleSLOT();
        }
    }

    @Override
    public DataFrame getPreparedDataToSend() {
        int fre = mFreFilter.getFre();
        assert ChannelManager.getInstance().isDataChannel(fre);
        mCurrentSendingFrame.setFre(fre);
        return mCurrentSendingFrame;
    }

    public DataFrame getDataToSend(){
        return mCurrentSendingFrame;
    }

    public void setCoolTimeNow(){
        this.lastCoolingTime = TimeController.getInstance().getCurrentTime();
    }

    public double getLastCoolingTime(){
        return this.lastCoolingTime;
    }

    public void setAcceptFre(int fre){
        mFreFilter.setFre(fre);
    }

    public Sender getSender(){
        return mSender;
    }

    public Receiver getReceiver(){
        return mReceiver;
    }

    public List<Frame> getReceivingFrames(){
        return receivingFrames;
    }


}
