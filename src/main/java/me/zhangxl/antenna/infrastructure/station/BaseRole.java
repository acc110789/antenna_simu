package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.DataFrame;

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

        //sender发送了PTS之后,以及某接收端接受到RTS之后,恰好这个RTS的target是自己
        //在上述两种情况下接收到任何frame都会由WAITING_PTS跳转到RECEIVING_PTS
        RECEIVING_PTS(true),

        //正在接受东西 有可能是receive RTS或者PTS,其它的类型的receive在这种状态忽略
        //
        RECEIVING(true),
        NAVING,

        WAITING_DATA,
        RECEIVING_DATA(true),
        SENDING_DATA(Mode.WRITE_MODE),

        WAITING_ACK,
        RECEIVING_ACK(true),
        SENDING_ACK(Mode.WRITE_MODE),


        COOLING, //没有接受东西了,但是仍然冷却一段时间(DIFS,或者EIFS)才能回到SLOTING的状态
        ;

        private final Mode mode;
        private boolean isReceiving = false;

        private Status(Mode mode){
            this.mode = mode;
        }
        private Status(){
            this(Mode.READ_MODE);
        }

        private Status(boolean isReceiving){
            this();
            this.isReceiving = isReceiving;
        }

        public boolean isReceiving(){
            return this.isReceiving;
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

    void setCommunicationTarget(int id);

    int getCommunicationTarget();
}
