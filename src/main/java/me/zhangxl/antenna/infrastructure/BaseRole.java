package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
interface BaseRole {

    int defaultCommunicationTarget = -1;

    enum Mode {
        READ_MODE,
        WRITE_MODE
    }

    enum EndReason{
        SUCCESS,
        COLLISION,
        TIMEOUT,
        NAV_OVER
    }

    enum Status {
        IDLE(false,Mode.READ_MODE), //可以假设成为 WAITING_RTS的timeout是DIFS,超时的话就进入SLOTING
        IDLE_RECEIVING(false,Mode.READ_MODE),

        SLOTING(false,Mode.READ_MODE),
        //note: 没有WAITING_RTS这个状态
        SENDING_RTS(true,Mode.WRITE_MODE),
        RECEIVING_RTS(false,Mode.READ_MODE),
        WAITING_CTS(true,Mode.READ_MODE),

        SENDING_SIFS_CTS(false,Mode.WRITE_MODE),
        SENDING_CTS(false,Mode.WRITE_MODE),
        RECEIVING_CTS(true,Mode.READ_MODE),
        WAITING_DATA(false,Mode.READ_MODE),

        SENDING_SIFS_DATA(true,Mode.WRITE_MODE),
        SENDING_DATA(true,Mode.WRITE_MODE),
        RECEIVING_DATA(false,Mode.READ_MODE),
        WAITING_ACK(true,Mode.READ_MODE),

        SENDING_SIFS_ACK(false,Mode.WRITE_MODE),
        SENDING_ACK(false,Mode.WRITE_MODE),
        RECEIVING_ACK(true,Mode.READ_MODE),


        NAV(false,null);

        final Boolean sender;
        final Mode mode;
        Status(Boolean sender,Mode mode){
            this.sender = sender;
            this.mode = mode;
        }

        boolean isSender(){
            return this.sender;
        }

        boolean isReadMode(){
            return mode == Mode.READ_MODE;
        }
    }

    Mode getCurrentMode();

    void setReadMode();

    void setWriteMode();

    void setCurrentStatus(Status status);

    Status getCurrentStatus();

    DataFrame getDataToSend();

    int getId();

    /**
     * 表明通信已经完毕,以下情况会使得通信完毕
     * (1)通信成功,则通信双方应该reset通信目标
     *      对于发送方来说 {@link SenderExpandRole#onPostRecvACK(AckFrame)}
     *      对于接受方来说 {@link ReceiverExpandRole#onPostSendACK()}
     * (2)通信失败,对于一方来说是遭受到了碰撞,对于另一方来说是超时没有收到期待的frame
     *      碰撞发生的地点 {@link Station#scheduleLatestCollisionFrame(Frame)}
     *      超时的地方 {@link SenderExpandRole#onPostSendRTS()}
     *                {@link ReceiverExpandRole#onPostSendCTS()}
     *                {@link SenderExpandRole#onPostSendDATA()}
     * (3)NAV结束
     * 通信完毕之后有几件事情是需要注意的:
     * (1)如果通信失败,则对于发送方来说需要将碰撞次数加一,扩大碰撞窗口.接收方则不受影响
     * (2)如果是 超时性质的通信失败,则要马上执行{@link Station#onPostDIFS()}
     * 否则需要等DIFS之后才能执行{@link Station#onPostDIFS()}
     * @param success
     * @param fail
     */
    void onPostCommunication(boolean success, boolean fail);

    /**
     * 当发生timeOut的时候,不能立刻进行DIFS,因为当前有可能正在接受无用的frame
     * 应该
     */
    void onPostTimeOut();

    void setCommunicationTarget(int id);

    int getCommunicationTarget();
}
