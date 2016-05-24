package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
public interface BaseRole {

    int defaultCommunicationTarget = -1;

    enum Mode{
        READ_MODE,
        WRITE_MODE
    }

    enum Status {
        SLOTING,
        //SLOTING之后是SENDING_RTS
        SENDING_RTS(Mode.WRITE_MODE),
        //只有发送者发送完RTS之后 或者 接收者收到了发送者发出的RTS
        //这两种情况下发送者和接收者分别进入这种状态
        WAITING_PTS,
        NAVING,

        WAITING_DATA,
        SENDING_DATA(Mode.WRITE_MODE),

        WAITING_ACK,
        SENDING_ACK(Mode.WRITE_MODE),

        RECEIVING,//正在接受东西
        COOLING, //没有接受东西了,但是仍然冷却一段时间(DIFS,或者EIFS)才能回到SLOTING的状态
        ;

        private final Mode mode;

        private Status(Mode mode){
            this.mode = mode;
        }
        private Status(){
            this(Mode.READ_MODE);
        }

        boolean isReadMode(){
            return this.mode == Mode.READ_MODE;
        }

        boolean isWriteMode(){
            return this.mode == Mode.WRITE_MODE;
        }
    }

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
     *      碰撞发生的地点 {@link Station#(Frame)}
     *      超时的地方 {@link SenderExpandRole#onPostSendRTS()}
     *                {@link ReceiverExpandRole#onPostSendCTS()}
     *                {@link SenderExpandRole#onPostSendDATA()}
     * (3)NAV结束
     * 通信完毕之后有几件事情是需要注意的:
     * (1)如果通信失败,则对于发送方来说需要将碰撞次数加一,扩大碰撞窗口.接收方则不受影响
     * (2)如果是 超时性质的通信失败,则要马上执行{@link Station#onPostDIFS()}
     * 否则需要等DIFS之后才能执行{@link Station#onPostDIFS()}
     * @param isSender
     *
     */
    void endCommunication(boolean isSender);

    void setCommunicationTarget(int id);

    int getCommunicationTarget();
}
