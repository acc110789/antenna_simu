package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;

/**
 * 指一次通信过程中的接受者角色
 * Created by zhangxiaolong on 16/4/15.
 */
class Receiver extends BaseRoleFilter implements ReceiverExpandRole {

    private static final Logger logger = new Logger(Receiver.class);
    private final ReceiveBaseRole mRole;

    Receiver(ReceiveBaseRole role) {
        super(role);
        this.mRole = role;
    }

    @Override
    public void onPreSendSIFSAndCTS(final RtsFrame frame) {
        logger.log("%d onPreSendSIFSAndCTS()", getId());
        assert getCurrentMode() == Mode.READ_MODE;
        setWriteMode();
        assert getCurrentStatus() == Status.RECEIVING_RTS;
        setCurrentStatus(Status.SENDING_SIFS_CTS);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendCTS(frame.generateCtsFrame());
            }
        }, Config.getInstance().getSifs());
    }

    @Override
    public void onPreSendCTS(CtsFrame frame) {
        logger.log("%d onPreSendCTS()", getId());
        assert getCurrentMode() == Mode.WRITE_MODE;
        assert getCurrentStatus() == Status.SENDING_SIFS_CTS;
        setCurrentStatus(Status.SENDING_CTS);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendCTS();
            }
        }, frame.getTransmitDuration());
        sendFrame(frame);
    }

    @Override
    public void onPostSendCTS() {
        logger.log("%d onPostSendCTS()", getId());
        assert getCurrentMode() == Mode.WRITE_MODE;
        setReadMode();
        assert getCurrentStatus() == Status.SENDING_CTS;
        setCurrentStatus(Status.WAITING_DATA);
        //设置CTS超时,如果超时,直接判定为碰撞
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (getCurrentStatus() == Status.WAITING_DATA) {
                    logger.log("station :%d after onPostSendCTS(),wait data timeout", getId());
                    onPostCommunication(true, true);
                }
            }
        }, DataFrame.getDataTimeOut());
    }

    @Override
    public void onPreSendSIFSAndACK(final AckFrame frame) {
        logger.log("%d onPreSendSIFSAndACK()", getId());
        assert getCurrentMode() == Mode.READ_MODE;
        setWriteMode();
        assert getCurrentStatus() == Status.RECEIVING_DATA;
        setCurrentStatus(Status.SENDING_SIFS_ACK);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendAck(frame);
            }
        }, Config.getInstance().getSifs());
    }

    @Override
    public void onPreSendAck(AckFrame frame) {
        logger.log("%d onPreSendAck()", getId());
        assert getCurrentMode() == Mode.WRITE_MODE;
        assert getCurrentStatus() == Status.SENDING_SIFS_ACK;
        setCurrentStatus(Status.SENDING_ACK);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendACK();
            }
        }, frame.getTransmitDuration());
        sendFrame(frame);
    }

    @Override
    public void onPostSendACK() {
        logger.log("%d onPostSendACK()", getId());
        assert getCurrentMode() == Mode.WRITE_MODE;
        setReadMode();
        onPostCommunication(false, false);
    }

    @Override
    public void onPreRecvRTS(final RtsFrame frame) {
        if (getCommunicationTarget() == BaseRole.defaultCommunicationTarget) {
            assert getCurrentMode() == Mode.READ_MODE;
            assert getCurrentStatus() == Status.IDLE;
            setCurrentStatus(Status.RECEIVING_RTS);
            String infoToLog;
            if (frame.getTargetId() == getId()) {
                infoToLog = String.format("%d onPreRecvRTS()", getId());
                setCommunicationTarget(frame.getSrcId());
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        onPostRecvRTS(frame);
                    }
                }, frame.getTransmitDuration());
            } else {
                //如果对方的通信目标不是自己,则设置NAV
                infoToLog = String.format("%d received irrelevant frame onPreRecvRTS()", getId());
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if (!frame.collision()) {
                            setNAV();
                            logger.log("%d blocked due to NAV set", getId());
                            TimeController.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    unsetNAV();
                                    logger.log("%d unblocked", getId());
                                    onPostCommunication(false,false);
                                }
                            }, frame.getNavDuration());
                        }
                    }
                }, frame.getTransmitDuration());
            }
            logger.log(infoToLog);
        } else {
            logger.log("%d receive a non collision rts ," +
                    "but already in a communication process,just ignore it", getId());
        }
    }

    @Override
    public void onPostRecvRTS(RtsFrame frame) {
        if (!frame.collision()) {
            logger.log("%d onPostRecvRTS()", getId());
            onPreSendSIFSAndCTS(frame);
        }
    }

    @Override
    public void onPreRecvData(final DataFrame dataFrame) {
        logger.log("%d onPreRecvData()", getId());
        if (dataFrame.getSrcId() == getCommunicationTarget()) {
            assert getCurrentMode() == Mode.READ_MODE;
            assert getCurrentStatus() == Status.WAITING_DATA;
            setCurrentStatus(Status.RECEIVING_DATA);
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvData(dataFrame);
                }
            }, dataFrame.getTransmitDuration());
        } else {
            logger.log("%d receive a non collision data ," +
                    "but already in a communication process,just ignore it", getId());
        }
    }

    /**
     * {@link #onPreSendSIFSAndACK(AckFrame)}
     */
    @Override
    public void onPostRecvData(DataFrame frame) {
        if (!frame.collision()) {
            logger.log("%d onPostRecvData()", getId());
            onPreSendSIFSAndACK(frame.generateAckFrame());
        }
    }

    @Override
    public void setNAV() {
        mRole.setNAV();
    }

    @Override
    public void unsetNAV() {
        mRole.unsetNAV();
    }

    @Override
    public boolean inNAV() {
        return mRole.inNAV();
    }
}
