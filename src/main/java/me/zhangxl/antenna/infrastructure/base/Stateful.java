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
        /**
         * 需要等待DIFS才能进入backoff时的状态
         */
        IDLE1(false, Mode.READ_MODE), //可以假设成为 WAITING_RTS的timeout是DIFS,超时的话就进入SLOTING
        /**
         * 需要等待EIFS才能进入backoff的状态
         */
        IDLE2(false, Mode.READ_MODE),//IDLE1 需要等待DIFS,IDLE2需要等待EIFS
        /**
         * 此时节点没有通信者,但是正在接受数据,有可能这个数据是无效数据
         * 如果是无效数据,则进入IDLE2状态,如果是一个有效的RTS数据
         * 则进入RECEIVING_RTS
         */
        IDLE_RECEIVING(false, Mode.READ_MODE),

        SLOTING(false, Mode.READ_MODE),
        //note: 没有WAITING_RTS这个状态
        SENDING_RTS(true, Mode.WRITE_MODE),
        RECEIVING_RTS(false, Mode.READ_MODE),
        WAITING_CTS(true, Mode.READ_MODE),

        SENDING_SIFS_CTS(false, Mode.WRITE_MODE),
        SENDING_CTS(false, Mode.WRITE_MODE),
        RECEIVING_CTS(true, Mode.READ_MODE),
        WAITING_DATA(false, Mode.READ_MODE),

        SENDING_SIFS_DATA(true, Mode.WRITE_MODE),
        SENDING_DATA(true, Mode.WRITE_MODE),
        RECEIVING_DATA(false, Mode.READ_MODE),
        WAITING_ACK(true, Mode.READ_MODE),

        SENDING_SIFS_ACK(false, Mode.WRITE_MODE),
        SENDING_ACK(false, Mode.WRITE_MODE),
        RECEIVING_ACK(true, Mode.READ_MODE),

        COOLING(false,Mode.READ_MODE),
        NAV(false,null);

        final Boolean sender;
        final Mode mode;
        Status(Boolean sender,Mode mode){
            this.sender = sender;
            this.mode = mode;
        }

        public boolean isSender(){
            return this.sender;
        }

        public boolean isReadMode(){
            return mode == Mode.READ_MODE;
        }

        public boolean isWriteMode(){
            return mode == Mode.WRITE_MODE;
        }
    }

    void setCurrentStatus(Stateful.Status status);

    Stateful.Status getCurrentStatus();
}
