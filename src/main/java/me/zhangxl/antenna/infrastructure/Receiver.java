package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;

/**
 * 指一次通信过程中的接受者角色
 * 凡是收到的数据一定是没有遭受到碰撞的数据
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
        onSendMethod(String.format("%d onPreSendSIFSAndCTS()", getId()),
                Status.RECEIVING_RTS, Status.SENDING_SIFS_CTS,
                new Runnable() {
                    @Override
                    public void run() {
                        onPreSendCTS(frame.generateCtsFrame());
                    }
                },Config.getInstance().getSifs());
    }

    @Override
    public void onPreSendCTS(CtsFrame frame) {
        onSendMethod(String.format("%d onPreSendCTS()", getId()),
                Status.SENDING_SIFS_CTS, Status.SENDING_CTS,
                new Runnable() {
                    @Override
                    public void run() {
                        onPostSendCTS();
                    }
                },frame.getTransmitDuration());
        sendFrame(frame);
    }

    @Override
    public void onPostSendCTS() {
        onSendMethod(String.format("%d onPostSendCTS()", getId()),
                Status.SENDING_CTS, Status.WAITING_DATA,
                new Runnable() {
                    @Override
                    public void run() {
                        if (getCurrentStatus() == Status.WAITING_DATA) {
                            logger.log("station :%d after onPostSendCTS(),wait data timeout", getId());
                            onPostCommunication(false,true);
                        }
                    }
                },DataFrame.getDataTimeOut());
    }

    @Override
    public void onPreSendSIFSAndACK(final AckFrame frame) {
        onSendMethod(String.format("%d onPreSendSIFSAndACK()", getId()),
                Status.RECEIVING_DATA, Status.SENDING_SIFS_ACK,
                new Runnable() {
                    @Override
                    public void run() {
                        onPreSendAck(frame);
                    }
                },Config.getInstance().getSifs());
    }

    @Override
    public void onPreSendAck(AckFrame frame) {
        onSendMethod(String.format("%d onPreSendAck()", getId()),
                Status.SENDING_SIFS_ACK, Status.SENDING_ACK,
                new Runnable() {
                    @Override
                    public void run() {
                        onPostSendACK();
                    }
                },frame.getTransmitDuration());
        sendFrame(frame);
    }

    @Override
    public void onPostSendACK() {
        logger.log("%d onPostSendACK()", getId());
        assert getCurrentStatus() == Status.SENDING_ACK;
        onPostCommunication(true,false);
    }


    @Override
    public void onPostRecvRTS(RtsFrame frame) {
        logger.log("%d onPostRecvRTS()", getId());
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
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        onPostCommunication(false,false);
                    }
                },frame.getNavDuration());
            }
        } else {
            logger.log("currentStatus is :" + getCurrentStatus().toString() + "ignore");
        }
    }

    /**
     * {@link #onPreSendSIFSAndACK(AckFrame)}
     */
    @Override
    public void onPostRecvData(final DataFrame frame) {
        onPostRecvMethod(String.format("%d onPostRecvData()", getId()),
                frame, Status.WAITING_DATA, Status.RECEIVING_DATA,
                new Runnable() {
                    @Override
                    public void run() {
                        onPreSendSIFSAndACK(frame.generateAckFrame());
                    }
                });
    }

}
