package me.zhangxl.antenna.infrastructure.station;

/**
 * Created by zhangxiaolong on 16/5/25.
 */
public interface Stateful {
    void setCurrentStatus(Status status);
    Status getCurrentStatus();

    enum Status {
        SLOTING,
        //SLOTING之后是SENDING_RTS
        SENDING_RTS(BaseRole.Mode.WRITE_MODE),
        WAITING_RTS,
        //只有发送者发送完RTS之后 或者 接收者收到了发送者发出的RTS
        //这两种情况下发送者和接收者分别进入这种状态
        WAITING_PTS,

        SENDING_PTS(BaseRole.Mode.WRITE_MODE),

        //sender发送了PTS之后,以及某接收端接受到RTS之后,恰好这个RTS的target是自己
        //在上述两种情况下接收到任何frame都会由WAITING_PTS跳转到RECEIVING_PTS
        RECEIVING_PTS(true),

        //正在接受东西 有可能是receive RTS或者PTS,其它的类型的receive在这种状态忽略
        //
        RECEIVING(true),
        NAVING,

        WAITING_DATA,
        RECEIVING_DATA(true),
        SENDING_DATA(BaseRole.Mode.WRITE_MODE),

        WAITING_ACK,
        RECEIVING_ACK(true),
        SENDING_ACK(BaseRole.Mode.WRITE_MODE),


        COOLING, //没有接受东西了,但是仍然冷却一段时间(DIFS,或者EIFS)才能回到SLOTING的状态
        ;

        private final BaseRole.Mode mode;
        private boolean isReceiving = false;

        private Status(BaseRole.Mode mode){
            this.mode = mode;
        }
        private Status(){
            this(BaseRole.Mode.READ_MODE);
        }

        private Status(boolean isReceiving){
            this();
            this.isReceiving = isReceiving;
        }

        public boolean isReceiving(){
            return this.isReceiving;
        }

        public boolean isReadMode(){
            return this.mode == BaseRole.Mode.READ_MODE;
        }

        boolean isWriteMode(){
            return this.mode == BaseRole.Mode.WRITE_MODE;
        }
    }
}
