package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
public class Receiver extends FilterRole implements ReceiverRole {

    private static final Logger logger = new Logger(Receiver.class);

    public Receiver(Role role){
        super(role);
    }

    @Override
    public void onPreSendSIFSAndCTS(final RtsFrame frame) {
        assertCurrentMode(Role.READ_MODE);
        setWriteMode();
        logger.log("%d onPreSendSIFSAndCTS()",getId());
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendCTS(frame.generateCtsFrame());
            }
        }, Config.getInstance().getSifs());
        assertCurrentStatus(Role.Status.RECEIVING_RTS);
        setCurrentStatus(Status.SENDING_SIFS_CTS);
    }

    @Override
    public void onPreSendCTS(CtsFrame frame) {
        logger.log("%d onPreSendCTS()",getId());
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendCTS();
            }
        }, frame.getTransmitDuration());
        assertCurrentStatus(Role.Status.SENDING_SIFS_CTS);
        setCurrentStatus(Status.SENDING_CTS);
    }

    @Override
    public void onPostSendCTS() {
        assertCurrentMode(Role.WRITE_MODE);
        setReadMode();
        logger.log("%d onPostSendCTS()",getId());
        //设置CTS超时,如果超时,直接判定为碰撞
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (getCurrentStatus() == Role.Status.WAITING_DATA) {
                    logger.log("station :%d after onPostSendCTS(),wait data timeout",getId());
                    onPostCommunication(true, true);
                }
            }
        }, DataFrame.getDataTimeOut());
        assertCurrentStatus(Status.SENDING_CTS);
        setCurrentStatus(Status.WAITING_DATA);
    }

    @Override
    public void onPreSendSIFSAndACK(final AckFrame frame) {
        assertCurrentMode(Role.READ_MODE);
        setWriteMode();
        logger.log("%d onPreSendSIFSAndACK()",getId());
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendAck(frame);
            }
        }, Config.getInstance().getSifs());
        assertCurrentStatus(Role.Status.RECEIVING_DATA);
        setCurrentStatus(Status.SENDING_SIFS_ACK);
    }

    @Override
    public void onPreSendAck(AckFrame frame) {
        logger.log("%d onPreSendAck()",getId());
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendACK();
            }
        }, frame.getTransmitDuration());
        assertCurrentStatus(Status.SENDING_SIFS_ACK);
        setCurrentStatus(Status.SENDING_ACK);
    }

    @Override
    public void onPostSendACK() {
        assertCurrentMode(Role.WRITE_MODE);
        setReadMode();
        logger.log("%d onPostSendACK()",getId());
        onPostCommunication(false, false);
    }

    @Override
    public void onPreRecvRTS(final RtsFrame frame) {
        if(currentCommunicationTarget == defaultCommunicationTarget){
            if(frame.getTargetId() == getId()) {
                logger.log("%d onPreRecvRTS()",getId());
                currentCommunicationTarget = frame.getSrcId();
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        onPostRecvRTS(frame);
                    }
                }, frame.getTransmitDuration());
                assertCurrentStatus(Status.IDLE);
                setCurrentStatus(Status.RECEIVING_RTS);
            } else {
                //如果对方的通信目标不是自己,则设置NAV
                setNAV();
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        unsetNAV();
                        scheduleDIFS(false);
                    }
                },frame.getNavDuration());
            }
        } else {
            logger.log("%d receive a non collision rts ," +
                    "but already in a communication process,just ignore it",getId());
        }
    }

   @Override
    public void onPostRecvRTS(RtsFrame frame) {
        if (!frame.collision()) {
            logger.log("%d onPostRecvRTS()",getId());
            onPreSendSIFSAndCTS(frame);
        }
    }

    @Override
    public void onPreRecvData(final DataFrame dataFrame) {
        if(dataFrame.getSrcId() == currentCommunicationTarget) {
            logger.log("%d onPreRecvData()",getId());
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvData(dataFrame);
                }
            }, dataFrame.getTransmitDuration());
            assertCurrentStatus(Status.WAITING_DATA);
            setCurrentStatus(Status.RECEIVING_DATA);
        } else {
            logger.log("%d receive a non collision data ," +
                    "but already in a communication process,just ignore it",getId());
        }
    }

    /**
     * {@link #onPreSendSIFSAndACK(AckFrame)}
     */
    @Override
    public void onPostRecvData(DataFrame frame) {
        if (!frame.collision()) {
            logger.log("%d onPostRecvData()",getId());
            onPreSendSIFSAndACK(frame.generateAckFrame());
        }
    }
}
