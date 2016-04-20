package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.SimuLoggerManager;
import me.zhangxl.antenna.util.TimeLogger;
import org.apache.logging.log4j.Logger;

/**
 * 一次通信过程中的发送者角色
 * Created by zhangxiaolong on 16/4/15.
 */
class Sender extends BaseRoleFilter implements SenderExpandRole {

    private static final Logger logger = SimuLoggerManager.getLogger(Sender.class.getSimpleName());
    private final SendBaseRole mRole;

    Sender(SendBaseRole role){
        super(role);
        this.mRole = role;
    }

    @Override
    public void onPreSendRTS(RtsFrame frame) {
        onSendMethod(logger, String.format("%d onPreSendRTS()", getId()), Status.SLOTING,
                Status.SENDING_RTS, new Runnable() {
                    @Override
                    public void run() {
                        onPostSendRTS();
                    }
                }, frame.getTransmitDuration());
        setCommunicationTarget(frame.getTargetId());
        sendFrame(frame);
    }

    @Override
    public void onPostSendRTS() {
        onSendMethod(logger, String.format("%d onPostSendRTS()",getId()), Status.SENDING_RTS,
                Status.WAITING_CTS, new Runnable() {
                    @Override
                    public void run() {
                        if (getCurrentStatus() == Status.WAITING_CTS) {
                            logger.debug("%d after onPostSendRTS() wait CTS timeout",getId());
                            endCommunication(false,true);
                        }
                    }
                }, CtsFrame.getCtsTimeOut(), TimeTask.CTS_TIMEOUT);
    }

    @Override
    public void onPreSendSIFSAndDATA() {
        onSendMethod(logger, String.format("%d onPreSendSIFSAndDATA()",getId()), Status.RECEIVING_CTS,
                Status.SENDING_SIFS_DATA, new Runnable() {
                    @Override
                    public void run() {
                        onPreSendData(getDataToSend());
                    }
                }, Config.getInstance().getSifs());
    }

    @Override
    public void onPreSendData(DataFrame dataFrame) {
        onSendMethod(logger, String.format("%d onPreSendData()",getId()), Status.SENDING_SIFS_DATA,
                Status.SENDING_DATA, new Runnable() {
                    @Override
                    public void run() {
                        onPostSendDATA();
                    }
                }, dataFrame.getTransmitDuration());
        sendFrame(dataFrame);
    }

    @Override
    public void onPostSendDATA() {
        onSendMethod(logger, String.format("%d onPostSendDATA()",getId()), Status.SENDING_DATA,
                Status.WAITING_ACK, new Runnable() {
                    @Override
                    public void run() {
                        if (getCurrentStatus() == Status.WAITING_ACK) {
                            logger.debug("%d after onPostSendDATA(),wait ack timeout",getId());
                            endCommunication(false,true);
                        }

                    }
                }, AckFrame.getAckTimeOut(),TimeTask.ACK_TIMEOUT);
    }

    /**
     * 在真实的环境下,只有没有被碰撞的frame才能收到,所以当发生碰撞时,不打log
     * {@link #onPreSendSIFSAndDATA()}
     */
    @Override
    public void onPostRecvCTS(CtsFrame frame) {
        onPostRecvMethod(logger, String.format("%d onPostRecvCTS()", getId()),
                frame, Status.WAITING_CTS,
                Status.RECEIVING_CTS, new Runnable() {
                    @Override
                    public void run() {
                        onPreSendSIFSAndDATA();
                    }
                });
    }

    @Override
    public void onPostRecvACK(AckFrame frame){
        onPostRecvMethod(logger, String.format("%d onPostRecvACK()",getId()),
                frame, Status.WAITING_ACK,
                Status.RECEIVING_ACK, new Runnable() {
                    @Override
                    public void run() {
                        if (TimeLogger.DEBUG_STATION) {
                            logger.debug("%d send a data successfully...", getId());
                        }
                        endCommunication(true,false);
                    }
                });
    }

    @Override
    public void onSendSuccess() {
        mRole.onSendSuccess();
    }
}
