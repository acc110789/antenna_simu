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
        COOLING(false),
        SLOTING(false),
        //note: 没有WAITING_RTS这个状态
        SENDING_RTS(true, Mode.WRITE_MODE),
        RECEIVING_RTS(false),
        WAITING_CTS(true),

        SENDING_CTS(false, Mode.WRITE_MODE),
        RECEIVING_CTS(true),
        WAITING_DATA(false),

        SENDING_DATA(true, Mode.WRITE_MODE),
        RECEIVING_DATA(false),
        WAITING_ACK(true),

        SENDING_ACK(false, Mode.WRITE_MODE),
        RECEIVING_ACK(true),


        NAV(false, null);

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

    Stateful.Status getCurrentStatus();
}
