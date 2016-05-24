package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.cool.DifsCooler;
import me.zhangxl.antenna.infrastructure.station.wait.ReceiverPtsTimeOutWaiter;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

/**
 * 指一次通信过程中的接受者角色
 * 凡是收到的数据一定是没有遭受到碰撞的数据
 * Created by zhangxiaolong on 16/4/15.
 */
class Receiver extends BaseRoleFilter implements ReceiverExpandRole {
    private static final Logger logger = SimuLoggerManager.getLogger(Receiver.class.getSimpleName());
    private final Station mRole;
    private final double ptsTimeOut = PrecisionUtil.add(Config.getInstance().getSifs(),
            PrecisionUtil.mul(2, PtsFrame.getFrameTimeLength()),
            Config.getInstance().getDifs());

    Receiver(Station role) {
        super(role);
        this.mRole = role;
    }

    @Override
    public void onPostRecvRTS(RtsFrame frame) {
        logger.debug("%d onPostRecvRTS()", getId());
        if(frame.getTargetId() == mRole.getId()){
            //如果目标节点是自己,则停止slot,马上进入等待pts的阶段
            new ReceiverPtsTimeOutWaiter(mRole).await();
        } else {
            //RTS不是发给自己的,则设置NAV
            //不是发给本Station的,这种情况下应当设置NAV向量
                setCurrentStatus(Status.NAVING);
                logger.info("%d set NAV",getId());
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        logger.info("%d NAV finish",getId());
                        endCommunication(false);
                    }
                },frame.getNavDuration(),TimeTask.RECEIVE);
        }
    }

    private void onPreSendSIFSAndACK(final AckFrame frame) {
        onSendMethod(logger, String.format("%d onPreSendSIFSAndACK()", getId()), Status.SENDING_ACK,
                Status.SENDING_ACK, new Runnable() {
                    @Override
                    public void run() {
                        onPreSendAck(frame);
                    }
                }, Config.getInstance().getSifs(),TimeTask.SEND);
    }

    @Override
    public void onPreSendAck(AckFrame frame) {
        onSendMethod(logger, String.format("%d onPreSendAck()", getId()), Status.SENDING_ACK,
                Status.SENDING_ACK, new Runnable() {
                    @Override
                    public void run() {
                        onPostSendACK();
                    }
                }, frame.getTransmitDuration(),TimeTask.SEND);
        sendFrame(frame);
    }

    @Override
    public void onPostSendACK() {
        logger.debug("%d onPostSendACK()", getId());
        assert getCurrentStatus() == Status.SENDING_ACK;
        new DifsCooler(mRole).cool();
    }

    /**
     * {@link #onPreSendSIFSAndACK(AckFrame)}
     */
    @Override
    public void onPostRecvData(final DataFrame frame) {
        onPostRecvMethod(logger, String.format("%d onPostRecvData()", getId()), frame, Status.WAITING_DATA,
                Status.SENDING_ACK, new Runnable() {
                    @Override
                    public void run() {
                        onPreSendSIFSAndACK(frame.generateAckFrame());
                    }
                });
    }

}
