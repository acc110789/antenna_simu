package me.zhangxl.antenna.infrastructure.pcp;

import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.base.ChannelManager;
import me.zhangxl.antenna.infrastructure.base.Locatable;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.*;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 代表一个Pcp节点,Pcp节点起到掌控全局的作用,
 * 一个局域网中只有一个Pcp节点,总是让PCP节点
 * 处于坐标(0,0)的位置.
 * 整个局域网时间片的流动应该由本节点发送
 * {@link me.zhangxl.antenna.frame.NextRoundFrame}
 * 来进行驱动
 * Created by zhangxiaolong on 16/5/12.
 */
public class PcpStation implements Locatable {
    private static final Logger logger = SimuLoggerManager.getLogger("Pcp");
    private static PcpStation sInstance = new PcpStation();
    private final Pair<Double, Double> mLocation = new Pair<>(0.0, 0.0);
    private final List<RtsFrame> receivedRtss = new ArrayList<>();
    private final List<RtsFrame> receivingRtss = new ArrayList<>();
    private Status currentStatus = Status.WAITING_RTS;
    private final PcpFreFilter mFreFilter = new PcpFreFilter();
    private final ChannelUsage channelUsage = new ChannelUsage();
    private CollisionRecord collisionRecord = new CollisionRecord();

    /**
     * 上一组rts是空闲或者碰撞,如果是空闲,则把下一个slots增加1,
     * 如果是碰撞,则把下一个slot减1.
     */
    private int mSlots = 1;

    public static PcpStation getInstance() {
        return sInstance;
    }

    private PcpStation() {
        Medium.getInstance().register(this);
    }

    @Override
    public Pair<Double, Double> getAxis() {
        return mLocation;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void setCurrentStatus(Status status) {
        currentStatus = status;
    }

    @Override
    public Status getCurrentStatus() {
        return currentStatus;
    }

    /**
     * @param frame 从{@link Medium}发送过来的frame
     * @return 最终把收到的frame放到receivedRtss中
     */
    @Override
    public boolean beginReceiveFrame(final Frame frame) {
        //检查频率
        if (mFreFilter.canReceive(frame.getFre())) {
            //如果是rtsframe的频率,则进行对应的处理
            assert frame instanceof RtsFrame;
            //当前的状态一定是waiting rts
            assert getCurrentStatus() == Status.WAITING_RTS;
            //检查是否与已经存在的同频率的frame发生的碰撞
            for (RtsFrame frame1 : receivingRtss) {
                if (frame1.getFre() == frame.getFre()
                        && StationUtil.hasIntersection(frame1, frame)) {
                    frame1.setDirty();
                    frame.setDirty();
                }
            }
            receivingRtss.add((RtsFrame) frame);
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    assert currentStatus == Status.WAITING_RTS;
                    receivingRtss.remove(frame);
                    receivedRtss.add((RtsFrame) frame);
                }
            }, frame.getEndDuration(), TimeTask.RECEIVE);
        }
        return true;
    }

    /**
     * 程序真正开始loop的时候回首先由本节点发送
     * {@link me.zhangxl.antenna.frame.NextRoundFrame}
     * 然后设置定时器,等时间到了之后检查收到的RtsFrame,
     * 根据情况发送
     * {@link me.zhangxl.antenna.frame.PairFrame}
     * 完毕之后最后再次发送
     * {@link me.zhangxl.antenna.frame.NextRoundFrame}
     *
     * @param slots        NextRoundFrame允许的退避槽(slot)数量,如果大于或者等于0,则使用这个slots,
     *                     如果slots的值小于0,则使用 {@link #prepareSlot()}重置的slots值
     * @param doubleWindow 将上一轮发送Rts但是没有被xiangxin
     */
    public void sendNextRoundFrame(int slots, boolean doubleWindow) {
        receivingRtss.clear();
        receivedRtss.clear();
        logger.debug("%d onPreSendNextRoundFrame", getId());
        assert getCurrentStatus() == Status.WAITING_RTS || getCurrentStatus() == Status.SENDING_PAIR;
        if (slots < 0) {
            prepareSlot();
        } else {
            mSlots = slots;
        }
        logger.info("%d slots permitted", mSlots);
        final NextRoundFrame frame = new NextRoundFrame(getId(), -1, ChannelManager.getInstance().getPcpChannel(), mSlots);
        if (doubleWindow) {
            frame.setNeedRefresh();
        }
        logger.info("need double window : %s", doubleWindow ? "yes" : "no");
        Medium.getInstance().putFrame(this, frame);
        setCurrentStatus(Status.SENDING_NEXT_ROUND);

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                assert getCurrentStatus() == Status.SENDING_NEXT_ROUND;
                logger.debug("%d onPostSendNextRoundFrame", getId());
                setCurrentStatus(Status.WAITING_RTS);
                registerDealer();
            }
        }, frame.getTransmitDuration(), TimeTask.SEND);
    }

    /**
     * 当NextRoundFrame发送完毕的时候,需要注册对下一个Round
     * 期间收到的RtsFrame进行处理的dealer
     * 处理器有两个,分别是截止时间的处理器和超时的处理器
     */
    private void registerDealer() {
        //rts 已经超时之后都没有收到任何的rts应该如下处理
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (currentStatus == Status.WAITING_RTS) {
                    logger.info("timeout receive none rts frame");
                    //意味着在这期间没有周围没有任何节点发送RTS请求
                    sendNextRoundFrame(-1, false);
                }
            }
        }, Constant.getPcpRtsTimeOut(mSlots), TimeTask.AFTER_RECEIVE);

        //在规定时间内收到的RtsFrame应该作如下处理
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                //如果普通节点都完全遵守这个协议,那么在此刻,应该是没有正在接受的frame
                assert receivingRtss.size() == 0;
                if (receivedRtss.size() > 0) {
                    //如果收到了rts,则进入下一个流程,否则,等待直到rts超时,然后进入超时的处理流程
                    logger.info("begin deal received rtss");
                    setCurrentStatus(Status.SENDING_PAIR);
                    //分析是否发生了碰撞以及碰撞是否严重
                    collisionRecord.analyze(receivedRtss);
                    //去掉碰撞的rts
                    Iterator<RtsFrame> iter = receivedRtss.iterator();
                    while (iter.hasNext()) {
                        if (iter.next().isDirty()) {
                            iter.remove();
                        }
                    }
                    //开始处理这些收到的rts
                    onSendPairFrame();
                }
            }
        }, Constant.getPcpRtsDeadLine(mSlots), TimeTask.AFTER_RECEIVE);
    }

    /**
     * 真正在发送之前还需要休息一个sifs
     */
    private void onSendPairFrame() {
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                logger.debug("%d onSendPcpControlFrame", getId());
                sendPairFrameInner();
            }
        }, Config.getInstance().getSifs());
    }

    private void sendPairFrameInner() {
        if (!hasFreeDataChannel()) {
            //没有可用的dataChannel,则把所有的rts都清空,让所有的节点设置nav,直到至少有一个dataChannel可用
            receivedRtss.clear();
            logger.info("没有可用的data channel,向所有的节点发送nav");
            NavFrame frame = new NavFrame(0, -1, ChannelManager.getInstance().getPcpChannel());
            double navDuration = PrecisionUtil.sub(channelUsage.getShortestWaitTime(),
                    Constant.getNavFrameTimeLength());
            navDuration = Math.max(navDuration, 0);
            frame.setNavDuration(navDuration);
            final double finalNavDuration = navDuration;
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    TimeController.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            sendNextRoundFrame(0, false);
                        }
                    }, finalNavDuration);
                }
            }, Constant.getNavFrameTimeLength());
            Medium.getInstance().putFrame(this, frame);
            return;
        }
        //receivedRtss里面全都是没有被碰撞的桢,只要是size大于0,就是没有碰撞的桢
        if (receivedRtss.size() > 0) {
            final RtsFrame frame = getFreeRts();
            if (frame != null) {
                receivedRtss.remove(frame);
                final int channel = channelUsage.getNextFreeChannel();
                PairFrame pairFrame = new PairFrame(frame.getSrcId(), frame.getTargetId(),
                        ChannelManager.getInstance().getPcpChannel(), channel);
                //发送PairFrame
                logger.debug("%d onPreSendPairFrame", getId());
                Medium.getInstance().putFrame(this, pairFrame);
                logger.info("%d pairFrame,  srcId:%d,   targetId:%d,   channelId:%d",
                        getId(), frame.getSrcId(), frame.getTargetId(), channel);
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        //发送完毕之后紧接着发送下一个
                        logger.debug("%d onPostSendPairFrame", getId());
                        channelUsage.putCommunicatePair(frame.getSrcId(), frame.getTargetId(), channel);
                        //准备发送下一个可能PairFrame
                        onSendPairFrame();
                    }
                }, pairFrame.getTransmitDuration(), TimeTask.SEND);
            } else {
                RtsFrame rtsFrame = receivedRtss.remove(0);
                if (channelUsage.isReceiver(rtsFrame.getSrcId())) {
                    //说明在本轮早就已经成为另一对连接的接收者了
                    sendPairFrameInner();
                } else {
                    //向这个Frame的发送者回复NavFrame
                    NavFrame navFrame = new NavFrame(0, rtsFrame.getSrcId(), ChannelManager.getInstance().getPcpChannel());
                    double navDuration = channelUsage.getWaitingTimeNeeded(rtsFrame.getTargetId());
                    channelUsage.putNavItem(rtsFrame.getSrcId(), navDuration);

                    navDuration = PrecisionUtil.sub(navDuration, Constant.getNavFrameTimeLength());
                    navDuration = Math.max(navDuration, 0);
                    navFrame.setNavDuration(navDuration);
                    Medium.getInstance().putFrame(this, navFrame);
                    logger.debug("%d onPreSendNavFrame()", getId());
                    TimeController.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("%d onPostSendNavFrame()", getId());
                            onSendPairFrame();
                        }
                    }, Constant.getNavFrameTimeLength());
                }
            }
        } else {
            if (collisionRecord.collision()) {
                //发生了碰撞了
                if (collisionRecord.severeCollision()) {
                    //是严重的碰撞,窗口值加倍
                    sendNextRoundFrame(-1, true);
                } else {
                    sendNextRoundFrame(0, false);
                    //是一般的碰撞,窗口值不变
                }
            } else {
                sendNextRoundFrame(-1, false);
            }
        }
    }

    private void prepareSlot() {
        int busyNum = channelUsage.getItemSize();
        int peerNum = Medium.getInstance().getPeerNum();
        int freeNum = peerNum - busyNum;
        int window = Config.getInstance().getDefaultCW();
        int value;//一个free节点占有的window数量
        if (freeNum == 0) {
            // 当所有的节点都出于busy的状态的时候,直接将slot设置为window的一半
            mSlots = window / 2;
        } else {
            value = window / freeNum;
            value = Math.max(1, value);
            mSlots = value * (Config.getInstance().getRtsFreCount() - 1);
            //窗口值不能超过默认窗口的一半
            mSlots = Math.min(window / 2, mSlots);
        }
        if (mSlots < 0) {
            throw new IllegalStateException("mSlots is less than 0");
        }
    }

    /**
     * 从rtss中取一个frame出来,要保证这个frame的target不忙
     * frame的src也不忙。
     * 为什么要保证frame的src也不忙?因为上一个{@link #getFreeRts()}
     * 把src占据了。
     * 如果没有符合要求的frame,则返回null
     */
    private RtsFrame getFreeRts() {
        RtsFrame target = null;
        for (RtsFrame frame : receivedRtss) {
            //这里检查一下
            if (channelUsage.isIdFree(frame.getTargetId())
                    && channelUsage.isIdFree(frame.getSrcId())) {
                target = frame;
                break;
            }
        }
        return target;
    }

    /**
     * @return 还有空闲的DataChannel
     */
    private boolean hasFreeDataChannel() {
        try {
            channelUsage.getNextFreeChannel();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}

