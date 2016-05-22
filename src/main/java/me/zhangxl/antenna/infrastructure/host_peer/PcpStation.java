package me.zhangxl.antenna.infrastructure.host_peer;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.Locatable;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.station.StationUtil;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Pair;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 代表一个Pcp节点,Pcp节点起到掌控全局的作用,
 * 一个局域网中只有一个Pcp节点,总是让PCP节点
 * 处于坐标(0,0)的位置.
 * 整个局域网时间片的流动应该由本节点发送
 * 来进行驱动
 * Created by zhangxiaolong on 16/5/12.
 */
public class PcpStation implements Locatable {

    private static PcpStation sInstance = new PcpStation();
    private final Pair<Double, Double> mLocation = new Pair<>(0.0, 0.0);
    private final int id = 0;
    private final List<RtsFrame> receivingRtss = new ArrayList<>();
    private Status currentStatus = null;
    private static final Logger logger = SimuLoggerManager.getLogger(Station.class.getSimpleName());
    private RtsFrame currentDealingRts;

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
        if(currentStatus == Status.WAITING_RTS) {
            //保证当前接收的frame一定是RtsFrame
            assert frame instanceof RtsFrame;
            //检查是否与已经存在的frame发生任何的碰撞
            for (RtsFrame frame1 : receivingRtss) {
                if (StationUtil.hasIntersection(frame1,frame)) {
                    frame1.setDirty();
                    frame.setDirty();
                }
            }
            receivingRtss.add((RtsFrame) frame);
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    receivingRtss.remove(frame);
                    currentDealingRts = (RtsFrame) frame;
                    if(!frame.isDirty()) {
                        onPreSendSifsAndPts();
                    }
                }
            }, frame.getTransmitDuration(), TimeTask.RECEIVE);
            return true;
        } else {
            //包括两个sending data和nav
            return false;
        }
    }

    private void onPreSendSifsAndPts(){
        assert currentStatus == Status.WAITING_RTS;
        setCurrentStatus(Status.SENDING_PTS);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendPts();
            }
        },Config.getInstance().getSifs());
    }

    private void onPreSendPts(){
        assert currentStatus == Status.SENDING_PTS;
        final boolean passByPcp = false;
        //计算src和target之间的通信过程是否会经过Pcp节点
        // TODO: 16/5/21
        PtsFrame frame = new PtsFrame(currentDealingRts.getSrcId(),currentDealingRts.getTargetId(),passByPcp);
        // TODO: 16/5/21 这里应该是所有的扇区要挨着挨着发送
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendPts(passByPcp);
            }
        },frame.getTransmitDuration(),TimeTask.SEND);
    }

    private void onPostSendPts(boolean passByPcp){
        assert currentStatus == Status.SENDING_PTS;
        if(passByPcp){
            currentStatus = Status.NAVING;
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    assert currentStatus == Status.NAVING;
                    setCurrentStatus(Status.WAITING_RTS);
                }
                // TODO: 16/5/21 计算从NAV中恢复的时间
            },-1,TimeTask.SEND);
        }
    }

    private void setCurrentStatus(Status status) {
        currentStatus = status;
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
    NAVING,
    SENDING_PTS,
    WAITING_RTS,//就是idle状态了
}