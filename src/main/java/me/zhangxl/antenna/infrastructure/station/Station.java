package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.Locatable;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.infrastructure.station.receive_pair.OnReceivePtsFrame;
import me.zhangxl.antenna.util.Config;
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
    public final Sender mSender;
    final Receiver mReceiver;
    private Pair<Double, Double> mLocation; //定向天线时需要保证

    public DataFrame mCurrentSendingFrame;
    //wait list
    private List<DataFrame> mDataFramesToSend = new ArrayList<>();
    /**
     * 已经发送成功的frames
     */
    private List<DataFrame> mDataFrameSent = new ArrayList<>();
    /**
     * 正在接受的frames
     */
    public List<Frame> receivingFrames = new ArrayList<>();

    public Station(int id) {
        super(id);
        this.mSender = new Sender(this);
        this.mReceiver = new Receiver(this);
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

    /**
     * @return 获取此时等待着被发送的DataFrame的总量
     */
    int getWaitingRequestNum() {
        return mDataFramesToSend.size();
    }

    /**
     * 到了difs完毕的时刻,应该是该时刻的后半时刻,
     * 要求状态已经设置为SLOTING。
     * 一个Station的起始点就是这个函数。
     */
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
    private boolean sendDataIfNeed() {
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

    /**
     * 遭受到了碰撞
     */
    @Override
    void backOffDueToTimeout() {
        TimeController.getInstance().addCollitionTimes();
        mCurrentSendingFrame.addCollitionTimes();
    }

    void putDataFrame(int targetId, long length) {
        mDataFramesToSend.add(new DataFrame(getId(), targetId));
    }

    @Override
    public void onSendSuccess() {
        logger.info("%d send a data successfully",getId());
        TimeController.getInstance().addSuccessTimes();
        TimeController.getInstance().addDataAmount(mCurrentSendingFrame.getLength() / 8);
        mDataFrameSent.add(mCurrentSendingFrame);
        mCurrentSendingFrame = null;
    }

    /**
     * @param frame Station接受的桢从理论上说是不可能发生任何冲突的
     * @return
     */
    public boolean beginReceiveFrame(Frame frame){
        //如果频率不是这个station care的频率,就跟这个频率好像不存在是一样的
        if(getCurrentStatus() == Status.NAVING || !getCurrentStatus().isReadMode()){
            return false;
        }
        for (Frame frame1 : receivingFrames) {
            if (StationUtil.hasIntersection(frame1,frame)) {
                frame1.setDirty();
                frame.setDirty();
            }
        }
        receivingFrames.add(frame);

        if(frame instanceof RtsFrame){
            new OnReceiveRtsFrame(this).doLogic(frame);
        } else if(frame instanceof PtsFrame){
            new OnReceivePtsFrame(this).doLogic(frame);
        } else if(frame instanceof DataFrame){
            new OnReceiveDataFrame(this).doLogic(frame);
        } else if(frame instanceof  AckFrame){
            new OnReceiveAckFrame(this).doLogic(frame);
        } else {
            throw new IllegalStateException();
        }
        return true;
    }

    @Override
    public DataFrame getDataToSend() {
        return mCurrentSendingFrame;
    }


}
