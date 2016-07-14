package me.zhangxl.antenna.infrastructure.base;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
public interface Stateful {
    enum Mode {
        READ_MODE,
        WRITE_MODE
    }

    enum Status {
        //这几个状态是普通节点的几个状态啊
        WAITING_NEXT_ROUND(false),
        RECEIVING_PAIR_FRAME(false),
        RECEIVING_NAV_FRAME(false),
        RECEIVING_NEXT_ROUND_FRAME(false),
        SLOTING(false),
        //note: 没有WAITING_RTS这个状态
        SENDING_RTS(true, Mode.WRITE_MODE),
        WAITING_DATA(false),
        SENDING_DATA(true, Mode.WRITE_MODE),
        RECEIVING_DATA(false),
        WAITING_ACK(true),
        SENDING_ACK(false, Mode.WRITE_MODE),
        RECEIVING_ACK(true),
        NAV(false, null),

        //这几个状态在这个分支应该是不会被用到的
        COOLING(false),
        RECEIVING_RTS(false),
        WAITING_CTS(true),
        SENDING_CTS(false, Mode.WRITE_MODE),
        RECEIVING_CTS(true),

        //下面的状态是PCP节点的时间状态
        WAITING_RTS(false),
        SENDING_PAIR(false),
        SENDING_NEXT_ROUND(false);

        final Boolean sender;
        final Mode mode;

        Status(Boolean sender, Mode mode) {
            this.sender = sender;
            this.mode = mode;
        }

        Status(Boolean sender) {
            this.sender = sender;
            this.mode = Mode.READ_MODE;
        }

        public boolean isReadMode() {
            return mode == Mode.READ_MODE;
        }

        public boolean isWriteMode() {
            return mode == Mode.WRITE_MODE;
        }

        public boolean isSender(){
            return this.sender;
        }
    }

    void setCurrentStatus(Status status);

    Status getCurrentStatus();
}
