package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.timeout.WaitAckTimeOut;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

/**
 * 一次通信过程中的发送者角色
 * Created by zhangxiaolong on 16/4/15.
 */
public class Sender extends BaseRoleFilter implements SenderRole {

    private static final Logger logger = SimuLoggerManager.getLogger(Sender.class.getSimpleName());

    Sender(Station role){
        super(role);
    }

    @Override
    public void onPreSendRTS(RtsFrame frame) {
        onSendMethod(logger, String.format("%d onPreSendRTS()  channel:%d", getId(),frame.getFre()),
                Status.SLOTING,
                Status.SENDING_RTS,
                new Runnable() {
                    @Override
                    public void run() {
                        onPostSendRTS();
                    }
                }, frame.getTransmitDuration(),TimeTask.SEND);
        setCommunicationTarget(frame.getTargetId());
        sendFrame(frame);
    }

    private void onPostSendRTS() {
        onSendMethod(logger, String.format("%d onPostSendRTS()",getId()),
                Status.SENDING_RTS,
                Status.WAITING_NEXT_ROUND,
                null, -1, -1);
    }

    @Override
    public void onPreSendSIFSAndDATA() {
        onSendMethod(logger, String.format("%d onPreSendSIFSAndDATA()",getId()),
                Status.RECEIVING_PAIR_FRAME,
                Status.SENDING_DATA,
                new Runnable() {
                    @Override
                    public void run() {
                        onPreSendData(getPreparedDataToSend());
                    }
                }, Config.getInstance().getSifs(),TimeTask.SEND);
    }

    private void onPreSendData(DataFrame dataFrame) {
        onSendMethod(logger, String.format("%d onPreSendData()  channel:%d",getId(),dataFrame.getFre()),
                Status.SENDING_DATA,
                Status.SENDING_DATA,
                new Runnable() {
                    @Override
                    public void run() {
                        onPostSendDATA();
                    }
                }, dataFrame.getTransmitDuration(),TimeTask.SEND);
        sendFrame(dataFrame);
    }

    private void onPostSendDATA() {
        onSendMethod(logger, String.format("%d onPostSendDATA()",getId()),
                Status.SENDING_DATA,
                Status.WAITING_ACK,
                null,-1);
        new WaitAckTimeOut(station).await();
    }

    @Override
    public void onPostRecvACK(AckFrame frame){
        logger.debug("%d onPostRecvACK()",getId());
        assert getCurrentStatus() == Status.RECEIVING_ACK;
        station.onSendSuccess();
        onFinish();
    }
}
