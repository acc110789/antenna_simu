package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.Config;

/**
 * 对于全向天线来说,所有的Statin的状态都是同步的.
 * 但是对于定向天线来说,如果一个DataFrame的发送
 * 过程是成功的,则至少发送方和接收方的状态应该是同步的.
 * 整个发送和接受过程如下:
 * Source :        |<---DIFS--->|<--RTS-->|                  |<-SIFS->|<----DATA---->|
 * Destination:                           |<-SIFS->|<--CTS-->|                       |<-SIFS->|<--ACK-->|
 * Third:                                                                                               |<---DIFS--->|<NEWSLOT>|......
 * 上述 DIFS RTS SIFS 等都是一个过程
 * 在Stateful的方法中,用pre表示过程的开始的那一刻,用post表示过程结束的那一刻
 * Created by zhangxiaolong on 16/4/9.
 */
abstract class Stateful {
    // TODO: 16/4/10 暂时没有考虑两件事情在同一时间点发生的概率是0
    static final int READ_MODE = 1;
    static final int WRITE_MODE = 2;

    //需要注意的是一旦一个Station进入了写(发送)模式之后,
    //这个Station是不能进行读(接受)操作的,或者说即使Meduim
    //通知我有一个Frame,我不会对这个Frame做出任何的相应
    int currentMode = READ_MODE;

    DataFrame mCurrentSendingFrame;

    Status currentStatus = Status.IDLE;

    enum Status {
        IDLE,
        //note: 没有WAITING_RTS这个状态
        SENDING_RTS,
        RECEIVING_RTS,
        WAITING_CTS,

        SENDING_SIFS_CTS,
        SENDING_CTS,
        RECEIVING_CTS,
        WAITING_DATA,

        SENDING_SIFS_DATA,
        SENDING_DATA,
        RECEIVING_DATA,
        WAITING_ACK,

        SENDING_SIFS_ACK,
        SENDING_ACK,
        RECEIVING_ACK,
    }

    private void setReadMode() {
        if(this.currentMode != WRITE_MODE){
            throw new IllegalStateException("interesting, already in read mode");
        }
        this.currentMode = READ_MODE;
        Medium.getInstance().notify((Station) this);
    }

    protected boolean isReadMode() {
        return currentMode == READ_MODE;
    }

    protected boolean isWriteMode() {
        return currentMode == WRITE_MODE;
    }

    private void setWriteMode() {
        if(this.currentMode == WRITE_MODE){
            throw new IllegalStateException("interesting, already in write mode");
        }
        this.currentMode = WRITE_MODE;
    }

    /**
     * 遭受到了碰撞
     */
    public abstract void backOffDueToTimeout();

    public abstract void scheduleDIFS(boolean Immediate);

    public abstract void onPostDIFS();

    public abstract void scheduleSLOT();

    public abstract void onPostSLOT();

    void assertCurrentStatus(Status status){
        if(currentStatus != status){
            throw new IllegalStateException("currentStatus is not " + status);
        }
    }

    void assertCurrentMode(int mode){
        if(currentMode != mode){
            if(mode == READ_MODE) {
                throw new IllegalStateException("currentMode is not READ MODE");
            } else if(mode == WRITE_MODE){
                throw new IllegalStateException("currentMode is not WRITE MODE");
            } else {
                throw new IllegalStateException("neither READ MODE nor write mode");
            }
        }
    }

    //<editor-fold desc="发送数据时间点函数">
    /**
     * 这个时刻表示是RTS开始的那一刻,立刻进入写模式
     * @param frame 待发送的RTS
     */
    protected void onPreSendRTS(RtsFrame frame) {
        setWriteMode();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendRTS();
            }
        }, frame.getTransmitDuration());
        assertCurrentStatus(Status.IDLE);
        currentStatus = Status.SENDING_RTS;
    }

    private void onPostSendRTS() {
        setReadMode();
        //设置RTS超时时间,如果超时,则直接判定为碰撞
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(currentStatus == Status.WAITING_CTS) {
                    backOffDueToTimeout();
                }
            }
        }, CtsFrame.getCtsTimeOut());
        assertCurrentStatus(Status.SENDING_RTS);
        currentStatus = Status.WAITING_CTS;
    }

    /**
     * 这个时刻表示是CTS之前的的SIFS开始的时刻,在这个时刻,
     * Station进入了写模式
     */
    private void onPreSendSIFSAndCTS(final RtsFrame frame) {
        setWriteMode();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendCTS(frame.generateCtsFrame());
            }
        }, Config.getInstance().getSifs());
        assertCurrentStatus(Status.RECEIVING_RTS);
        currentStatus = Status.SENDING_SIFS_CTS;
    }

    /**
     * 刚刚开始发送CTS的那个时刻
     * @param frame 待发送的CTS
     */
    void onPreSendCTS(CtsFrame frame) {
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendCTS();
            }
        }, frame.getTransmitDuration());
        assertCurrentStatus(Status.SENDING_SIFS_CTS);
        currentStatus = Status.SENDING_CTS;
    }

    /**
     * CTS 刚刚发送完毕的那个时刻
     */
    private void onPostSendCTS() {
        setReadMode();
        //设置CTS超时,如果超时,直接判定为碰撞
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(currentStatus == Status.WAITING_DATA){
                    // TODO: 16/4/10 CTS超时之后就进入backOff
                    backOffDueToTimeout();
                }
            }
        }, DataFrame.getDataTimeOut());
        assertCurrentStatus(currentStatus = Status.SENDING_CTS);
        currentStatus = Status.WAITING_DATA;
    }

    private void onPreSendSIFSAndDATA() {
        setWriteMode();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendData(mCurrentSendingFrame);
            }
        }, Config.getInstance().getSifs());
        assertCurrentStatus(Status.RECEIVING_CTS);
        currentStatus = Status.SENDING_SIFS_DATA;
    }

    void onPreSendData(DataFrame dataFrame) {
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendDATA();
            }
        }, dataFrame.getTransmitDuration());
        assertCurrentStatus(Status.SENDING_SIFS_DATA);
        currentStatus = Status.SENDING_DATA;
    }

    private void onPostSendDATA() {
        setReadMode();
        //设置Data的Timeout,过时当碰撞处理
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(currentStatus == Status.WAITING_ACK) {
                    backOffDueToTimeout();
                }
            }
        }, AckFrame.getAckTimeOut());
        assertCurrentStatus(currentStatus = Status.SENDING_DATA);
        currentStatus = Status.WAITING_ACK;
    }

    private void onPreSendSIFSAndACK(final AckFrame frame) {
        setWriteMode();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendAck(frame);
            }
        }, Config.getInstance().getSifs());
        assertCurrentStatus(Status.RECEIVING_DATA);
        currentStatus = Status.SENDING_SIFS_ACK;
    }

    void onPreSendAck(AckFrame frame) {
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendACK();
            }
        }, frame.getTransmitDuration());
        assertCurrentStatus(currentStatus = Status.SENDING_SIFS_ACK);
        currentStatus = Status.SENDING_ACK;
    }

    private void onPostSendACK() {
        setReadMode();
        currentStatus = Status.IDLE;
        // TODO: 16/4/10 DIFS的监控,怎么开始下一个DIFS
    }
    //</editor-fold>

    private boolean canRecv() {
        // TODO: 16/4/11 思考如何才能保证可以接受
        return currentMode == READ_MODE;
    }

    //<editor-fold desc="接受数据时间点函数">
    /**
     * 开始接受rts frame
     * @param frame 即将被接受的frame
     */
    void onPreRecvRTS(final RtsFrame frame) {
        if (canRecv()) {
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvRTS(frame);
                }
            }, frame.getTransmitDuration());
            assertCurrentStatus(Status.IDLE);
            currentStatus = Status.RECEIVING_RTS;
        }
    }

    /**
     * {@link #onPreSendSIFSAndCTS(RtsFrame)} 与这个方法是在同一时间点,直接调用这个方法即可
     */
    private void onPostRecvRTS(RtsFrame frame) {
        if(!frame.collision()) {
            onPreSendSIFSAndCTS(frame);
        }
    }

    void onPreRecvCTS(final CtsFrame frame) {
        if (canRecv()) {
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvCTS(frame);
                }
            }, frame.getTransmitDuration());
            assertCurrentStatus(Status.WAITING_CTS);
            currentStatus = Status.RECEIVING_CTS;
        }
    }

    /**
     * {@link Stateful#onPreSendSIFSAndDATA()}
     */
    private void onPostRecvCTS(CtsFrame frame) {
        if(!frame.collision()) {
            onPreSendSIFSAndDATA();
        }
    }

    void onPreRecvData(final DataFrame dataFrame) {
        if (canRecv()) {
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvData(dataFrame);
                }
            }, dataFrame.getTransmitDuration());
            assertCurrentStatus(Status.WAITING_DATA);
            currentStatus = Status.RECEIVING_DATA;
        }
    }

    /**
     * {@link #onPreSendSIFSAndACK(AckFrame)}
     */
    private void onPostRecvData(DataFrame frame) {
        if(!frame.collision()) {
            onPreSendSIFSAndACK(frame.generateAckFrame());
        }
    }

    void onPreRecvACK(final AckFrame frame) {
        if (canRecv()) {
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvACK(frame);
                }
            }, frame.getTransmitDuration());
            assertCurrentStatus(Status.WAITING_ACK);
            currentStatus = Status.RECEIVING_ACK;
        }
    }

    /**
     * //表明发送成功了
     * @param frame
     */
    abstract void onPostRecvACK(AckFrame frame);

    //</editor-fold>

}
