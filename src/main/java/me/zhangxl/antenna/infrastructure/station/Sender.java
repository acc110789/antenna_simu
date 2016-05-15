package me.zhangxl.antenna.infrastructure.station;

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
        onSendMethod(logger, String.format("%d onPreSendRTS()  channel:%d", getId(),frame.getFre()),
                Status.SLOTING, Status.SENDING_RTS, new Runnable() {
                    @Override
                    public void run() {
                        onPostSendRTS();
                    }
                }, frame.getTransmitDuration(),TimeTask.SEND);
        setCommunicationTarget(frame.getTargetId());
        sendFrame(frame);
    }

    @Override
    public void onPostSendRTS() {
        onSendMethod(logger, String.format("%d onPostSendRTS()",getId()), Status.SENDING_RTS,
                Status.WAITING_NEXT_ROUND, null, -1, -1);
    }

    @Override
    public void onPreSendSIFSAndDATA() {
        onSendMethod(logger, String.format("%d onPreSendSIFSAndDATA()",getId()), Status.RECEIVING_PAIR_FRAME,
                Status.SENDING_DATA, new Runnable() {
                    @Override
                    public void run() {
                        onPreSendData(getDataToSend());
                    }
                }, Config.getInstance().getSifs(),TimeTask.SEND);
    }

    @Override
    public void onPreSendData(DataFrame dataFrame) {
        onSendMethod(logger, String.format("%d onPreSendData()  channel:%d",getId(),dataFrame.getFre()),
                Status.SENDING_DATA, Status.SENDING_DATA, new Runnable() {
                    @Override
                    public void run() {
                        onPostSendDATA();
                    }
                }, dataFrame.getTransmitDuration(),TimeTask.SEND);
        sendFrame(dataFrame);
    }

    @Override
    public void onPostSendDATA() {
        onSendMethod(logger, String.format("%d onPostSendDATA()",getId()), Status.SENDING_DATA,
                Status.WAITING_ACK, new Runnable() {
                    @Override
                    public void run() {
                        //超时,对方没有发送ACK,应该不可能执行到这里
                        if (getCurrentStatus() == Status.WAITING_ACK) {
                            logger.info("%d after onPostSendDATA(),wait ack timeout",getId());
                            // TODO: 16/5/13
                            throw new IllegalStateException("timeout when waiting ack");
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
        throw new IllegalStateException();
    }

    @Override
    public void onPostRecvACK(AckFrame frame){
        onPostRecvMethod(logger, String.format("%d onPostRecvACK()",getId()),
                frame, Status.RECEIVING_ACK,
                Status.RECEIVING_ACK, new Runnable() {
                    @Override
                    public void run() {
                        if (TimeLogger.DEBUG_STATION) {
                            logger.debug("%d send a data successfully...", getId());
                        }
                        endCommunication(true);
                    }
                });
    }

    @Override
    public void onSendSuccess() {
        mRole.onSendSuccess();
    }
}
