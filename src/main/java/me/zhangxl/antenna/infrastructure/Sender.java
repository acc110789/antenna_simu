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
public class Sender extends FilterRole implements SenderRole {

    private static final Logger logger = new Logger(Sender.class);

    public Sender(Role role){
        super(role);
    }

    @Override
    public void onPreSendRTS(RtsFrame frame) {
        assertCurrentMode(Role.READ_MODE);
        setWriteMode();
        logger.log("%d onPreSendRTS()",getId());
        currentCommunicationTarget = frame.getTargetId();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendRTS();
            }
        }, frame.getTransmitDuration());
        assertCurrentStatus(Role.Status.IDLE);
        setCurrentStatus(Role.Status.SENDING_RTS);
    }

    @Override
    public void onPostSendRTS() {
        assertCurrentMode(Role.WRITE_MODE);
        setReadMode();
        logger.log("%d onPostSendRTS()",getId());
        //设置RTS超时时间,如果超时,则直接判定为碰撞
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (getCurrentStatus() == Role.Status.WAITING_CTS) {
                    logger.log("%d after onPostSendRTS() wait CTS timeout",getId());
                    onPostCommunication(true,true);
                }
            }
        }, CtsFrame.getCtsTimeOut());
        assertCurrentStatus(Role.Status.SENDING_RTS);
        setCurrentStatus(Status.WAITING_CTS);
    }

    @Override
    public void onPreSendSIFSAndDATA() {
        assertCurrentMode(Role.READ_MODE);
        setWriteMode();
        logger.log("%d onPreSendSIFSAndDATA()",getId());
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendData(getDataToSend());
            }
        }, Config.getInstance().getSifs());
        assertCurrentStatus(Status.RECEIVING_CTS);
        setCurrentStatus(Status.SENDING_SIFS_DATA);
    }

    @Override
    public void onPreSendData(DataFrame dataFrame) {
        logger.log("%d onPreSendData()",getId());
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendDATA();
            }
        }, dataFrame.getTransmitDuration());
        assertCurrentStatus(Status.SENDING_SIFS_DATA);
        setCurrentStatus(Status.SENDING_DATA);
    }

    @Override
    public void onPostSendDATA() {
        assertCurrentMode(Role.WRITE_MODE);
        setReadMode();
        logger.log("%d onPostSendDATA()",getId());
        //设置Data的Timeout,过时当碰撞处理
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (getCurrentStatus() == Role.Status.WAITING_ACK) {
                    logger.log("%d after onPostSendDATA(),wait ack timeout",getId());
                    onPostCommunication(true , true);
                }
            }
        }, AckFrame.getAckTimeOut());
        assertCurrentStatus(Status.SENDING_DATA);
        setCurrentStatus(Status.WAITING_ACK);
    }

    @Override
    public void onPreRecvCTS(final CtsFrame frame) {
        if(frame.getSrcId() == currentCommunicationTarget){
            logger.log("%d onPreRecvCTS()",getId());
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvCTS(frame);
                }
            }, frame.getTransmitDuration());
            assertCurrentStatus(Status.WAITING_CTS);
            setCurrentStatus(Status.RECEIVING_CTS);
        } else {
            logger.log("%d receive a non collision cts ,but already" +
                    " in a communication process,just ignore it",getId());
        }

    }

    /**
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
        if(frame.getSrcId() == currentCommunicationTarget) {
            logger.log("%d onPreRecvACK()",getId());
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvACK(frame);
                }
            }, frame.getTransmitDuration());
            assertCurrentStatus(Status.WAITING_ACK);
            setCurrentStatus(Status.RECEIVING_ACK);
        } else {
            logger.log("%d receive a non collision ack ," +
                    "but already in a communication process,just ignore it",getId());
        }
    }

    @Override
    public void onPostRecvACK(AckFrame frame){
        logger.log("%d onPostRecvACK()",getId());
    }
}
