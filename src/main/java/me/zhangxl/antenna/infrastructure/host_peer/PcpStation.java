package me.zhangxl.antenna.infrastructure.host_peer;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.Locatable;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.medium.DirectMedium;
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
    /**
     * Pcp节点仅仅涉及到3种状态,分别是
     * 1. NAV
     * 2. WAITING_RTS
     * 3. SENDING_PTS
     */
    private Status currentStatus = Status.WAITING_RTS;
    private static final Logger logger = SimuLoggerManager.getLogger(Station.class.getSimpleName());
    private RtsFrame currentDealingRts;
    private DirectMedium.Info pcpInfo;

    private DirectMedium.Info getPcpInfo() {
        if (pcpInfo == null) {
            pcpInfo = DirectMedium.getPcpInfo();
        }
        return pcpInfo;
    }

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
        if(getCurrentStatus() == Status.NAVING || !getCurrentStatus().isReadMode()){
            return false;
        }
        if (currentStatus == Status.WAITING_RTS) {
            //保证当前接收的frame一定是RtsFrame
            assert frame instanceof RtsFrame;
            //检查是否与已经存在的frame发生任何的碰撞
            for (RtsFrame frame1 : receivingRtss) {
                if (StationUtil.hasIntersection(frame1, frame)) {
                    frame1.setDirty();
                    frame.setDirty();
                }
            }
            receivingRtss.add((RtsFrame) frame);
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    receivingRtss.remove(frame);
                    if (!frame.isDirty()) {
                        currentDealingRts = (RtsFrame) frame;
                        onPreSendSifsAndPts();
                    }
                    //如果是dirty的话,直接ignore,仅仅处理clean的frame
                }
            }, frame.getEndDuration(), TimeTask.RECEIVE);
            return true;
        } else {
            //包括两个sending data和nav
            return false;
        }
    }

    private void onPreSendSifsAndPts() {
        assert currentStatus == Status.WAITING_RTS;
        setCurrentStatus(Status.SENDING_PTS);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendPts();
            }
        }, Config.getInstance().getSifs());
    }

    private void onPreSendPts() {
        assert currentStatus == Status.SENDING_PTS;
        //计算src和target之间的通信过程是否会经过Pcp节点
        final boolean passByPcp = DirectMedium.cPass(currentDealingRts.getSrcId(),
                currentDealingRts.getTargetId(), getId());
        sendPtsToSrc(passByPcp);
    }

    private int getIndexOfId(int id) {
        return getPcpInfo().getIndex(id);
    }

    /**
     * @param passByPcp 将PTS发送给src所在的扇区(即RTSFrame的sender所在的山区)
     */
    private void sendPtsToSrc(final boolean passByPcp) {
        PtsFrame frame = new PtsFrame(currentDealingRts.getSrcId(),
                currentDealingRts.getTargetId(), passByPcp,
                Config.getInstance().getPart() - 1);

        final int srcSector = getIndexOfId(currentDealingRts.getSrcId());
        final int targetSector = getIndexOfId(currentDealingRts.getTargetId());

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(srcSector != targetSector) {
                    sendPtsToTarget(passByPcp);
                } else {
                    sendPtsToOthers(passByPcp,getOthersSector());
                }
            }
        }, frame.getTransmitDuration(), TimeTask.SEND);
        if(srcSector == targetSector) {
            logger.info("PcpStation begin send pts to src and target sector: %d",srcSector);
        } else {
            logger.info("PcpStation begin send pts to src sector: %d",srcSector);
        }
        Medium.getInstance().putFrame(this, frame, srcSector);
    }

    /**
     * @param passByPcp 将PTS发送给target所在的扇区
     */
    private void sendPtsToTarget(final boolean passByPcp) {
        PtsFrame frame = new PtsFrame(currentDealingRts.getSrcId(),
                currentDealingRts.getTargetId(), passByPcp,
                Config.getInstance().getPart() - 2);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                sendPtsToOthers(passByPcp,getOthersSector());
            }
        }, frame.getTransmitDuration(), TimeTask.SEND);

        int targetSector = getIndexOfId(currentDealingRts.getTargetId());
        logger.info("PcpStation begin send pts to target sector: %d",targetSector);
        Medium.getInstance().putFrame(this, frame,targetSector);
    }

    private List<Integer> getOthersSector(){
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < Config.getInstance().getPart(); i++) {
            list.add(i);
        }
        list.remove(getIndexOfId(currentDealingRts.getSrcId()));
        list.remove(getIndexOfId(currentDealingRts.getTargetId()));
        return list;
    }

    /**
     * @param passByPcp
     * @param sectorToSend
     * 将PTS发送到剩余的扇区
     */
    private void sendPtsToOthers(final boolean passByPcp, final List<Integer> sectorToSend) {
        if(sectorToSend.size() == 0){
            onPostSendPts(passByPcp);
            return;
        }
        PtsFrame frame = new PtsFrame(currentDealingRts.getSrcId(),
                currentDealingRts.getTargetId(), passByPcp,
                sectorToSend.size() - 1);
        Integer sector = sectorToSend.remove(0);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                sendPtsToOthers(passByPcp,sectorToSend);
            }
        },frame.getTransmitDuration(),TimeTask.SEND);
        logger.debug("pcp send pts to sector: %d",sector);
        Medium.getInstance().putFrame(this,frame,sector);
    }

    private void onPostSendPts(boolean passByPcp) {
        logger.debug("Pcp onPostSendRts");
        assert currentStatus == Status.SENDING_PTS;
        if (passByPcp) {
            currentStatus = Status.NAVING;
            logger.debug("Pcp setNav");
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    assert currentStatus == Status.NAVING;
                    logger.debug("Pcp unSetNav");
                    setCurrentStatus(Status.WAITING_RTS);
                }
            }, PtsFrame.getBaseNav(), TimeTask.SEND);
        }
    }

    public void setCurrentStatus(Status status) {
        currentStatus = status;
    }

    @Override
    public Status getCurrentStatus() {
        return currentStatus;
    }
}
