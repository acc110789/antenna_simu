package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

/**
 * 指一次通信过程中的接受者角色
 * 凡是收到的数据一定是没有遭受到碰撞的数据
 * Created by zhangxiaolong on 16/4/15.
 */
class Receiver extends BaseRoleFilter implements ReceiverExpandRole {

    private static final Logger logger = SimuLoggerManager.getLogger(Receiver.class.getSimpleName());
    private final ReceiveBaseRole mRole;

    Receiver(ReceiveBaseRole role) {
        super(role);
        this.mRole = role;
    }

    @Override
    public void onPreSendSIFSAndCTS(final RtsFrame frame) {
        onSendMethod(logger, String.format("%d onPreSendSIFSAndCTS()", getId()), Status.RECEIVING_RTS,
                Status.SENDING_SIFS_CTS, new Runnable() {
                    @Override
                    public void run() {
                        onPreSendCTS(frame.generateCtsFrame());
                    }
                }, Config.getInstance().getSifs(),TimeTask.SEND);
    }

    @Override
    public void onPreSendCTS(CtsFrame frame) {
        onSendMethod(logger, String.format("%d onPreSendCTS()", getId()), Status.SENDING_SIFS_CTS,
                Status.SENDING_CTS, new Runnable() {
                    @Override
                    public void run() {
                        onPostSendCTS();
                    }
                }, frame.getTransmitDuration(),TimeTask.SEND);
        sendFrame(frame);
    }

    @Override
    public void onPostSendCTS() {
        onSendMethod(logger, String.format("%d onPostSendCTS()", getId()), Status.SENDING_CTS,
                Status.WAITING_DATA, new Runnable() {
                    @Override
                    public void run() {
                        if (getCurrentStatus() == Status.WAITING_DATA) {
                            logger.info("station :%d after onPostSendCTS(),wait data timeout", getId());
                            endCommunication(false,true);
                        }
                    }
                }, DataFrame.getDataTimeOut(),TimeTask.DATA_TIMEOUT);
    }

    @Override
    public void onPreSendSIFSAndACK(final AckFrame frame) {
        onSendMethod(logger, String.format("%d onPreSendSIFSAndACK()", getId()), Status.RECEIVING_DATA,
                Status.SENDING_SIFS_ACK, new Runnable() {
                    @Override
                    public void run() {
                        onPreSendAck(frame);
                    }
                }, Config.getInstance().getSifs(),TimeTask.SEND);
    }

    @Override
    public void onPreSendAck(AckFrame frame) {
        onSendMethod(logger, String.format("%d onPreSendAck()", getId()), Status.SENDING_SIFS_ACK,
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
        endCommunication(true,false);
    }


    @Override
    public void onPostRecvRTS(RtsFrame frame) {
        logger.debug("%d onPostRecvRTS()", getId());
        if(getCurrentStatus() == Status.IDLE_RECEIVING){
            assert getCommunicationTarget() == defaultCommunicationTarget;
            if(frame.getTargetId() == getId()){
                //的确是发给本Station的,则开启会话
                setCommunicationTarget(frame.getSrcId());
                setCurrentStatus(Status.RECEIVING_RTS);
                onPreSendSIFSAndCTS(frame);
            } else {
                //不是发给本Station的,这种情况下应当设置NAV向量
                setCurrentStatus(Status.NAV);
                logger.info("%d set NAV",getId());
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        logger.info("%d NAV finish",getId());
                        endCommunication(false,false);
                    }
                },frame.getNavDuration(),TimeTask.RECEIVE);
            }
        } else {
            logger.debug("%d ignore RTS because currentStatus is :%s",getId(),getCurrentStatus().toString());
        }
    }

    /**
     * {@link #onPreSendSIFSAndACK(AckFrame)}
     */
    @Override
    public void onPostRecvData(final DataFrame frame) {
        onPostRecvMethod(logger,
                String.format("%d onPostRecvData()", getId()), frame, Status.WAITING_DATA,
                Status.RECEIVING_DATA, new Runnable() {
                    @Override
                    public void run() {
                        onPreSendSIFSAndACK(frame.generateAckFrame());
                    }
                });
    }

}
