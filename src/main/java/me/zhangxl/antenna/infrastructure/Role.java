package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
public interface Role {

    int READ_MODE = 1;
    int WRITE_MODE = 2;

    enum Status {
        IDLE(null),
        //note: 没有WAITING_RTS这个状态
        SENDING_RTS(true),
        RECEIVING_RTS(false),
        WAITING_CTS(true),

        SENDING_SIFS_CTS(false),
        SENDING_CTS(false),
        RECEIVING_CTS(true),
        WAITING_DATA(false),

        SENDING_SIFS_DATA(true),
        SENDING_DATA(true),
        RECEIVING_DATA(false),
        WAITING_ACK(true),

        SENDING_SIFS_ACK(false),
        SENDING_ACK(false),
        RECEIVING_ACK(true);

        final Boolean sender;
        private Status(Boolean sender){
            this.sender = sender;
        }

        boolean isSender(){
            if(sender == null){
                throw new IllegalStateException("can not invoke this method when status is idle");
            }
            return this.sender;
        }
    }

    void setReadMode();

    void setWriteMode();

    void setCurrentStatus(Status status);

    Status getCurrentStatus();

    void assertCurrentStatus(Status status);

    void assertCurrentMode(int mode);

    DataFrame getDataToSend();

    int getId();

    /**
     * 表明通信已经完毕,有两种情况会使得通信完毕
     * (1)通信成功,则通信双方应该reset通信目标
     *      对于发送方来说 {@link #onPostRecvACK(AckFrame)}
     *      对于接受方来说 {@link #onPostSendACK()}
     * (2)通信失败,对于一方来说是遭受到了碰撞,对于另一方来说是超时没有收到期待的frame
     *      碰撞发生的地点 {@link Station#scheduleLatestCollisionFrame(Frame)}
     *      超时的地方 {@link #onPostSendRTS()}
     *                {@link #onPostSendCTS()}
     *                {@link #onPostSendDATA()}
     *通信完毕之后有几件事情是需要注意的:
     * (1)如果通信失败,则对于发送方来说需要将碰撞次数加一,扩大碰撞窗口.接收方则不受影响
     * (2)如果是 超时性质的通信失败,则要马上执行{@link Station#onPostDIFS()}
     * 否则需要等DIFS之后才能执行{@link Station#onPostDIFS()}
     * @param fail 通信是否是失败的
     */
    void onPostCommunication(boolean fail, boolean timeout);

    /**
     * 遭受到了碰撞
     */
    void backOffDueToTimeout();

    void scheduleDIFS(boolean Immediate);

    void scheduleSLOT();

    void setNAV();

    void unsetNAV();

    boolean inNAV();

    void setCommunicationTarget(int id);

    int getCommunicationTarget();
}
