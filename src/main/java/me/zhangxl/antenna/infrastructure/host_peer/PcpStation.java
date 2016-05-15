package me.zhangxl.antenna.infrastructure.host_peer;

import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.ChannelManager;
import me.zhangxl.antenna.infrastructure.Locatable;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.infrastructure.station.Station;
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

    private static PcpStation sInstance = new PcpStation();
    private static final int initialSlot = 1;
    private final Pair<Double, Double> mLocation = new Pair<>(0.0, 0.0);
    private final int id = 0;
    private final List<RtsFrame> receivedRtss = new ArrayList<>();
    private final List<RtsFrame> receivingRtss = new ArrayList<>();
    private Status currentStatus = null;
    private final HostFreFilter mFreFilter = new HostFreFilter();
    private final ChannelUsage channelUsage = new ChannelUsage();
    private static final Logger logger = SimuLoggerManager.getLogger(Station.class.getSimpleName());
    /**
     * 上一组rts是空闲或者碰撞,如果是空闲,则把下一个slots增加1,
     * 如果是碰撞,则把下一个slot减1.
     */
    private int mSlots = initialSlot;

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
        return this.id;
    }

    @Override
    public boolean beginReceiveFrame(final Frame frame) {
        //检查频率
        if (mFreFilter.canReceive(frame.getFre())) {
            //如果是rtsframe的频率,则进行对应的处理
            try {
                assert frame instanceof RtsFrame;
            } catch (Throwable e){
                System.out.println(frame.getClass().getSimpleName());
            }
            //检查当前的状态必须是waiting rts
            assert currentStatus == Status.WAITING_RTS;
            //检查是否与已经存在的frame发生任何的碰撞
            for (RtsFrame frame1 : receivingRtss) {
                if (frame1.getFre() == frame.getFre()) {
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
            }, frame.getTransmitDuration(), TimeTask.RECEIVE);
        }
        return true;
    }

    private void setCurrentStatus(Status status){
        currentStatus = status;
    }

    /**
     * 程序真正开始loop的时候回首先由本节点发送
     * {@link me.zhangxl.antenna.frame.NextRoundFrame}
     * 然后设置定时器,等时间到了之后检查收到的RtsFrame,
     * 根据情况发送
     * {@link me.zhangxl.antenna.frame.PairFrame}
     * 完毕之后最后再次发送
     * {@link me.zhangxl.antenna.frame.NextRoundFrame}
     */
    public void sendNextRoundFrame() {
        logger.debug("%d onPreSendNextRoundFrame",getId());
        logger.info("%d slots permitted",mSlots);
        final NextRoundFrame frame = new NextRoundFrame(getId(), -1, ChannelManager.getInstance().getPcpChannel(), mSlots);
        Medium.getInstance().putFrame(this, frame);
        setCurrentStatus(Status.SENDING_NEXT_ROUND);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                logger.debug("%d onPostSendNextRoundFrame",getId());
                setCurrentStatus(Status.WAITING_RTS);

                //rts 已经超时之后都没有收到任何的rts应该如下处理
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if (currentStatus == Status.WAITING_RTS) {
                            //意味着在这期间没有周围没有任何节点发送RTS请求,因此应该把slot加大
                            mSlots++;
                            sendNextRoundFrame();
                        }
                    }
                }, Constant.getPcpRtsTimeOut(mSlots), TimeTask.AFTER_RECEIVE);

                //在规定时间内收到的RtsFrame应该作如下处理
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if (receivedRtss.size() > 0) {
                            setCurrentStatus(Status.SENDING_PAIR);
                            boolean collision = false;
                            Iterator<RtsFrame> iter = receivedRtss.iterator();
                            while (iter.hasNext()) {
                                if (iter.next().isDirty()) {
                                    collision = true;
                                    iter.remove();
                                }
                            }
                            if (collision) {
                                logger.info("collision encountered");
                                mSlots--;
                            }
                            onSendPairFrame();
                        }
                    }
                }, Constant.getPcpRtsDeadLine(mSlots), TimeTask.AFTER_RECEIVE);
            }
        }, frame.getTransmitDuration(), TimeTask.SEND);
    }

    /**
     * 真正在发送之前还需要休息一个sifs
     */
    private void onSendPairFrame() {
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                logger.debug("%d onSendPairFrame",getId());
                sendPairFrameInner();
            }
        }, Config.getInstance().getSifs());
    }

    private void sendPairFrameInner() {
        //没有可用的dataChannel,则把所有的rts都清空
        if (!hasFreeDataChannel()) {
            receivedRtss.clear();
        }
        if (receivedRtss.size() > 0) {
            final RtsFrame frame = getFreeRts();
            if (frame != null) {
                receivedRtss.remove(frame);
                final int channel = getADataChannel();
                PairFrame pairFrame = new PairFrame(frame.getSrcId(), frame.getTargetId(),
                        ChannelManager.getInstance().getPcpChannel(), channel);
                //发送PairFrame
                Medium.getInstance().putFrame(this, pairFrame);
                logger.debug("%d onPreSendPairFrame",getId());
                logger.info("%d pairFrame,  srcId:%d,   targetId:%d,   channelId:%d",
                        getId(),frame.getSrcId(),frame.getTargetId(),channel);
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        //把pairFrame发送完毕
                        logger.debug("%d onPostSendPairFrame",getId());
                        channelUsage.put(frame.getSrcId(), frame.getTargetId(), channel);
                        //准备发送下一个可能PairFrame
                        onSendPairFrame();
                    }
                }, pairFrame.getTransmitDuration(), TimeTask.SEND);
            } else {
                receivedRtss.clear();
                sendNextRoundFrame();
            }
        } else {
            sendNextRoundFrame();
        }
    }

    /**
     * 从rtss中取一个frame出来,要保证这个frame的target不忙
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
        } catch (EmptyException e) {
            return false;
        }
    }

    private int getADataChannel() {
        try {
            return channelUsage.getNextFreeChannel();
        } catch (EmptyException e) {
            throw new IllegalStateException(e);
        }
    }
}

/**
 * {@link PcpStation} 的状态
 * 状态转移图
 * <p>
 * sending_next_round --> waiting_rts --> sending_pair --> sending_next_round --> .........
 * sending_next_round --> ........
 */
enum Status {
    SENDING_PAIR,
    SENDING_NEXT_ROUND,
    WAITING_RTS,
}

/**
 * 保存着信道的使用情况
 */
class ChannelUsage {
    /**
     * 一对正在通信的节点保存的信息
     */
    private static class CommunicateItem {
        final int srcId;
        final int targetId;
        final int channelId;

        CommunicateItem(int srcId, int targetId, int channelId) {
            this.srcId = srcId;
            this.targetId = targetId;
            this.channelId = channelId;
        }

        boolean hasId(int id) {
            return srcId == id || targetId == id;
        }
    }

    private final List<CommunicateItem> items = new ArrayList<>();

    /**
     * @param id 代表一个Station,是Station的编号
     * @return id所代表的Station目前是否处于空闲状态, 即正在Communicate的src
     * 以及target都没有id代表的station
     */
    public boolean isIdFree(int id) {
        for (CommunicateItem item : items) {
            if (item.hasId(id)) {
                return false;
            }
        }
        return true;
    }

    public int getNextFreeChannel() throws EmptyException {
        for (int channel : ChannelManager.getInstance().getDataChannels()) {
            //简单当前的channel是不是free,如果是free就将这个channel返回
            boolean isTaken = false;
            for (CommunicateItem item : items) {
                if (item.channelId == channel) {
                    isTaken = true;
                    break;
                }
            }
            if (!isTaken) {
                return channel;
            }
        }
        throw new EmptyException();
    }

    public void put(int srcId, int targetId, int channelId) {
        final CommunicateItem item = new CommunicateItem(srcId, targetId, channelId);
        items.add(item);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                items.remove(item);
            }
        }, Constant.getDataChannelDeadLine(), TimeTask.AFTER_RECEIVE);
    }
}

class EmptyException extends Exception {
    public EmptyException() {
        super("no free channel left");
    }
}
