package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;

/**
 * 一次通信过程中的发送者角色
 * Created by zhangxiaolong on 16/4/15.
 */
class Sender extends RoleFilter implements SenderRole {

    private static final Logger logger = new Logger(Sender.class);
    private final SendPermitRole mRole;

    Sender(SendPermitRole role){
        super(role);
        this.mRole = role;
    }

    @Override
    public void onPreSendRTS(RtsFrame frame) {
        logger.log("%d onPreSendRTS()",getId());
        assertCurrentMode(READ_MODE);
        setWriteMode();
        assertCurrentStatus(Status.IDLE);
        setCurrentStatus(Status.SENDING_RTS);

        setCommunicationTarget(frame.getTargetId());

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendRTS();
            }
        }, frame.getTransmitDuration());

        sendFrame(frame);
    }

    @Override
    public void onPostSendRTS() {
        logger.log("%d onPostSendRTS()",getId());
        assertCurrentMode(WRITE_MODE);
        setReadMode();
        assertCurrentStatus(Status.SENDING_RTS);
        setCurrentStatus(Status.WAITING_CTS);
        //设置RTS超时时间,如果超时,则直接判定为碰撞
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (getCurrentStatus() == Status.WAITING_CTS) {
                    logger.log("%d after onPostSendRTS() wait CTS timeout",getId());
                    onPostCommunication(true,true);
                }
            }
        }, CtsFrame.getCtsTimeOut());
    }

    @Override
    public void onPreSendSIFSAndDATA() {
        logger.log("%d onPreSendSIFSAndDATA()",getId());
        assertCurrentMode(READ_MODE);
        setWriteMode();
        assertCurrentStatus(Status.RECEIVING_CTS);
        setCurrentStatus(Status.SENDING_SIFS_DATA);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendData(getDataToSend());
            }
        }, Config.getInstance().getSifs());

    }

    @Override
    public void onPreSendData(DataFrame dataFrame) {
        logger.log("%d onPreSendData()",getId());
        assertCurrentMode(WRITE_MODE);
        assertCurrentStatus(Status.SENDING_SIFS_DATA);
        setCurrentStatus(Status.SENDING_DATA);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendDATA();
            }
        }, dataFrame.getTransmitDuration());
        sendFrame(dataFrame);
    }

    @Override
    public void onPostSendDATA() {
        logger.log("%d onPostSendDATA()",getId());
        assertCurrentMode(WRITE_MODE);
        setReadMode();
        assertCurrentStatus(Status.SENDING_DATA);
        setCurrentStatus(Status.WAITING_ACK);
        //设置Data的Timeout,过时当碰撞处理
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (getCurrentStatus() == Status.WAITING_ACK) {
                    logger.log("%d after onPostSendDATA(),wait ack timeout",getId());
                    onPostCommunication(true , true);
                }
            }
        }, AckFrame.getAckTimeOut());
    }

    @Override
    public void onPreRecvCTS(final CtsFrame frame) {
        logger.log("%d onPreRecvCTS()",getId());
        if(frame.getSrcId() == getCommunicationTarget()){
            assertCurrentMode(READ_MODE);
            assertCurrentStatus(Status.WAITING_CTS);
            setCurrentStatus(Status.RECEIVING_CTS);

            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvCTS(frame);
                }
            }, frame.getTransmitDuration());

        } else {
            logger.log("%d begin receive a non collision cts ,but already" +
                    " in a communication process,will ignore it",getId());
        }
    }

    /**
     * 在真实的环境下,只有没有被碰撞的frame才能收到,所以当发生碰撞时,不打log
     * {@link #onPreSendSIFSAndDATA()}
     */
    @Override
    public void onPostRecvCTS(CtsFrame frame) {
        if (!frame.collision()) {
            logger.log("%d onPostRecvCTS()",getId());
            onPreSendSIFSAndDATA();
        }
    }

    @Override
    public void onPreRecvACK(final AckFrame frame) {
        logger.log("%d onPreRecvACK()",getId());
        if(frame.getSrcId() == getCommunicationTarget()) {
            assertCurrentMode(READ_MODE);
            assertCurrentStatus(Status.WAITING_ACK);
            setCurrentStatus(Status.RECEIVING_ACK);

            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvACK(frame);
                }
            }, frame.getTransmitDuration());
        } else {
            logger.log("%d begin receive a non collision ack ," +
                    "but already in a communication process,will ignore it",getId());
        }
    }

    @Override
    public void onPostRecvACK(AckFrame frame){
        if(!frame.collision()) {
            logger.log("%d onPostRecvACK()",getId());
            if (Logger.DEBUG_STATION) {
                logger.log("%d send a data successfully...", getId());
                logger.logln();
            }
            mRole.onSendSuccess();
            onPostCommunication(false, false);
        }
    }
}
