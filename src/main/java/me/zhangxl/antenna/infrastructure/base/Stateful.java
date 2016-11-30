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
        WAITING_BOF(false),
        RECEIVING_OTC(false),
        RECEIVING_SNAV(false),
        RECEIVING_BOF(false),
        SLOTING(false),
        //note: 没有WAITING_RTS这个状态
        SENDING_DRTS(true, Mode.WRITE_MODE),
        WAITING_DDATA(false),
        SENDING_DDATA(true, Mode.WRITE_MODE),
        RECEIVING_DDATA(false),
        WAITING_DACK(true),
        SENDING_DACK(false, Mode.WRITE_MODE),
        RECEIVING_DACK(true),
        NAV(false, null),

        COOLING(false),

        //下面的状态是PCP节点的时间状态
        WAITING_DRTS(false),
        PCP_PROCESSING(false),
        SENDING_BOF(false);

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
