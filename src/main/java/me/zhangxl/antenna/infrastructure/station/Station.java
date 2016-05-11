package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.ChannelManager;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.medium.Medium;
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
public class Station extends AbstractRole{

    private static final Logger logger = SimuLoggerManager.getLogger(Station.class.getSimpleName());
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
        ChannelManager.getInstance();
    }

    public Station(int id, double xAxis, double yAxis) {
        this(id);
        this.mLocation = new Pair<>(xAxis, yAxis);
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

    @Override
    void onFinish() {
        setCommunicationTarget(defaultCommunicationTarget);
        Medium.getInstance().notify(this);
        for(Frame frame : receivingFrames){
            frame.setDirty();
        }
        if(receivingFrames.isEmpty()){
            setCurrentStatus(Status.IDLE1);
        } else {
            setCurrentStatus(Status.IDLE_RECEIVING);
        }
    }

    @Override
    public void onPostDIFS() {
        logger.debug("%d onPostDIFS", getId());
        assert getCurrentStatus() == Status.SLOTING;
        if (mCurrentSendingFrame == null) {
            getDataFrameToSend();
        } else {
            logger.info("%d current window: %d",getId(),mCurrentSendingFrame.getBackOff());
        }
        sendDataIfNeed();
        if(getCurrentStatus() == Status.SLOTING){
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
        sendDataIfNeed();
        if(getCurrentStatus() == Status.SLOTING){
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

    private void sendDataIfNeed() {
        if (mCurrentSendingFrame != null && mCurrentSendingFrame.canBeSent()) {
            //开始进入流程
            if (TimeLogger.DEBUG_STATION) {
                logger.debug("%d start transmit data frame sendDataIfNeed", this.getId());
            }
            mCurrentSendingFrame.setStartTimeNow();
            mSender.onPreSendRTS(mCurrentSendingFrame.generateRtsFrame());
        }
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
        if(!getCurrentStatus().isReadMode()){
            return false;
        }
        for(Frame frame1 : receivingFrames){
            if(StationUtil.hasIntersection(frame1,frame)){
                frame.setDirty();
                frame1.setDirty();
            }
        }
        receivingFrames.add(frame);
        if(getCurrentStatus() == Status.SLOTING ||
                getCurrentStatus() == Status.IDLE1 ||
                getCurrentStatus() == Status.IDLE2){
            setCurrentStatus(Status.IDLE_RECEIVING);
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
                assert getCurrentStatus() != Status.IDLE2;
                assert getCurrentStatus() != Status.IDLE1;
                assert getCurrentStatus() != Status.SLOTING;
                receivingFrames.remove(frame);
                if(!frame.isDirty()) {
                    //接收成功
                    if (frame instanceof RtsFrame) {
                        mReceiver.onPostRecvRTS((RtsFrame) frame);
                    } else if (frame instanceof CtsFrame) {
                        mSender.onPostRecvCTS((CtsFrame) frame);
                    } else if (frame instanceof DataFrame) {
                        mReceiver.onPostRecvData((DataFrame) frame);
                    } else if (frame instanceof AckFrame) {
                        mSender.onPostRecvACK((AckFrame) frame);
                    } else {
                        throw new IllegalArgumentException("unspecified frame type " + frame.getClass().getSimpleName());
                    }
                } else if(getCurrentStatus() == Status.IDLE_RECEIVING && receivingFrames.isEmpty()) {
                    //接收失败且当前状态处于IDLE_RECEIVING状态
                    setCurrentStatus(Status.IDLE2);
                }
                //接收失败且当前状态不是处于IDLE_RECEIVING的状态的时候就当作没有什么都没有发生过,上层发现timeout之后会自行处理
            }
        },frame.getEndDuration(),priority);
        return true;
    }

    @Override
    public DataFrame getDataToSend() {
        return mCurrentSendingFrame;
    }


}
