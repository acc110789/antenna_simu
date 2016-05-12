package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.NextRoundFrame;
import me.zhangxl.antenna.frame.PairFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.ChannelManager;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Pair;
import me.zhangxl.antenna.util.PrecisionUtil;

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
    private final List<RtsFrame> rtss = new ArrayList<>();
    private Status currentStatus = null;
    /**
     * 上一组rts是空闲或者碰撞,如果是空闲,则把下一个slots增加1,
     * 如果是碰撞,则把下一个slot减1.
     */
    private int mSlots = initialSlot;

    public static PcpStation getInstance() {
        return sInstance;
    }

    private PcpStation() {
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
    public boolean beginReceiveFrame(Frame frame) {
        //检查频率
        return false;
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
    private void sendNextRoundFrame() {
        final NextRoundFrame frame = new NextRoundFrame(getId(), -1, ChannelManager.getInstance().getPcpChannel(), mSlots);
        Medium.getInstance().putFrame(this, frame);
        currentStatus = Status.SENDING_NEXT_ROUND;
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                //rts 已经超时
                currentStatus = Status.WAITING_RTS;

                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if(currentStatus == Status.WAITING_RTS){
                            //意味着在这期间没有周围没有任何节点发送RTS请求,因此应该把slot加大
                            sendNextRoundFrame();
                        }
                    }
                }, PrecisionUtil.add(PrecisionUtil.mul(mSlots, Config.getInstance().getSlotLength()),
                        RtsFrame.getRtsTimeOut()), TimeTask.RECEIVE);

                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if(rtss.size() > 0){
                            currentStatus = Status.SENDING_PAIR;
                            boolean collision = false;
                            Iterator<RtsFrame> iter =  rtss.iterator();
                            while (iter.hasNext()){
                                if(iter.next().isDirty()){
                                    collision = true;
                                    iter.remove();
                                }
                            }
                            if(collision){
                                mSlots --;
                            }
                            onSendPairFrame();
                        } else {
                            //没有发现rts,把slot减少
                            mSlots ++;
                        }
                    }
                },PrecisionUtil.add(PrecisionUtil.mul(mSlots, Config.getInstance().getSlotLength()),
                        RtsFrame.getTimeLength()),TimeTask.RECEIVE);
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
                sendPairFrameInner();
            }
        },Config.getInstance().getSifs());
    }

    private void sendPairFrameInner(){
        if(rtss.size() > 0){
            RtsFrame frame = rtss.remove(0);
            PairFrame pairFrame = new PairFrame(frame.getSrcId(),frame.getTargetId(),
                    ChannelManager.getInstance().getPcpChannel());
            Medium.getInstance().putFrame(this,pairFrame);
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onSendPairFrame();
                }
            },pairFrame.getTransmitDuration(),TimeTask.RECEIVE);
        } else {
            sendNextRoundFrame();
        }
    }
}

/**
 * {@link PcpStation} 的状态
 * 状态转移图
 * <p>
 * sending_next_round --> waiting_rts --> sending_pair --> sending_next_round --> .........
 *                                        sending_next_round --> ........
 */
enum Status {
    SENDING_PAIR,
    SENDING_NEXT_ROUND,
    WAITING_RTS,
}
