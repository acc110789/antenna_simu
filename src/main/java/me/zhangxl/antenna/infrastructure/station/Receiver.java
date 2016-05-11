package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

/**
 * 指一次通信过程中的接受者角色
 * 凡是收到的数据一定是没有遭受到碰撞的数据
 * Created by zhangxiaolong on 16/4/15.
 */
public class Receiver extends BaseRoleFilter implements ReceiverRole {

    private static final Logger logger = SimuLoggerManager.getLogger(Receiver.class.getSimpleName());

    Receiver(Station station) {
        super(station);
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

    private void onPreSendAck(AckFrame frame) {
        onSendMethod(logger, String.format("%d onPreSendAck()", getId()), Status.SENDING_ACK,
                Status.SENDING_ACK, new Runnable() {
                    @Override
                    public void run() {
                        onPostSendACK();
                    }
                }, frame.getTransmitDuration(),TimeTask.SEND);
        sendFrame(frame);
    }

    private void onPostSendACK() {
        logger.debug("%d onPostSendACK()", getId());
        assert getCurrentStatus() == Status.SENDING_ACK;
        onFinish();
    }

    /**
     * {@link #onPreSendSIFSAndACK(AckFrame)}
     */
    @Override
    public void onPostRecvData(final DataFrame frame) {
        onPostRecvMethod(logger, String.format("%d onPostRecvData()", getId()), frame, Status.RECEIVING_DATA,
                Status.SENDING_ACK, new Runnable() {
                    @Override
                    public void run() {
                        onPreSendSIFSAndACK(frame.generateAckFrame());
                    }
                });
    }

}
