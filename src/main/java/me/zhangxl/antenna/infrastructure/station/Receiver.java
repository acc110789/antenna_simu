package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;
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
    public void onPreSendSIFSAndCTS(RtsFrame frame) {
        throw new IllegalStateException();
    }

    @Override
    public void onPreSendCTS(CtsFrame frame) {
        throw new IllegalStateException();
    }

    @Override
    public void onPostSendCTS() {
        throw new IllegalStateException();
    }

    @Override
    public void onPreSendSIFSAndACK(final AckFrame frame) {
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
        endCommunication(false);
    }


    @Override
    public void onPostRecvRTS(RtsFrame frame) {
        throw new IllegalStateException();
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
