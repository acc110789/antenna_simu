package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;

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
    private static Logger logger = new Logger(Stateful.class);

    final int id;
    static final int READ_MODE = 1;
    static final int WRITE_MODE = 2;

    boolean NAVING = false;

    //需要注意的是一旦一个Station进入了写(发送)模式之后,
    //这个Station是不能进行读(接受)操作的,或者说即使Meduim
    //通知我有一个Frame,我不会对这个Frame做出任何的相应
    int currentMode = READ_MODE;

    DataFrame mCurrentSendingFrame;

    Status currentStatus = Status.IDLE;

    private static int defaultCommunicationTarget = -1;
    /**
     * 节点的当前通信对象
     */
    int currentCommunicationTarget = defaultCommunicationTarget;

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

    Stateful(int id){
        this.id = id;
    }

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
    void onPostCommunication(boolean fail, boolean timeout){
        assert currentStatus != Status.IDLE;
        if(fail && currentStatus.isSender()){
            backOffDueToTimeout();
        }
        currentStatus = Status.IDLE;
        this.currentCommunicationTarget = defaultCommunicationTarget;
        scheduleDIFS(timeout);
    }

    private void setReadMode() {
        if (this.currentMode != WRITE_MODE) {
            throw new IllegalStateException("interesting, already in read mode");
        }
        this.currentMode = READ_MODE;
        Medium.getInstance().notify((Station) this);
    }

    private void setWriteMode() {
        this.currentMode = WRITE_MODE;
    }

    /**
     * 遭受到了碰撞
     */
    public abstract void backOffDueToTimeout();

    public abstract void scheduleDIFS(boolean Immediate);

    public abstract void scheduleSLOT();

    void assertCurrentStatus(Status status) {
        if (currentStatus != status) {
            throw new IllegalStateException("currentStatus is not " + status);
        }
    }

    private void assertCurrentMode(int mode) {
        if (currentMode != mode) {
            if (mode == READ_MODE) {
                throw new IllegalStateException("currentMode is not READ MODE");
            } else if (mode == WRITE_MODE) {
                throw new IllegalStateException("currentMode is not WRITE MODE");
            } else {
                throw new IllegalStateException("neither READ MODE nor write mode");
            }
        }
    }

    //<editor-fold desc="发送数据时间点函数">

    /**
     * 这个时刻表示是RTS开始的那一刻,立刻进入写模式
     *
     * @param frame 待发送的RTS
     */
    protected void onPreSendRTS(RtsFrame frame) {
        assertCurrentMode(READ_MODE);
        setWriteMode();
        logger.log("%d onPreSendRTS()",id);
        currentCommunicationTarget = frame.getTargetId();
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
        assertCurrentMode(WRITE_MODE);
        setReadMode();
        logger.log("%d onPostSendRTS()",id);
        //设置RTS超时时间,如果超时,则直接判定为碰撞
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (currentStatus == Status.WAITING_CTS) {
                    logger.log("%d after onPostSendRTS() wait CTS timeout",id);
                    onPostCommunication(true,true);
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
        assertCurrentMode(READ_MODE);
        setWriteMode();
        logger.log("%d onPreSendSIFSAndCTS()",id);
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
     *
     * @param frame 待发送的CTS
     */
    void onPreSendCTS(CtsFrame frame) {
        logger.log("%d onPreSendCTS()",id);
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
        assertCurrentMode(WRITE_MODE);
        setReadMode();
        logger.log("%d onPostSendCTS()",id);
        //设置CTS超时,如果超时,直接判定为碰撞
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (currentStatus == Status.WAITING_DATA) {
                    logger.log("station :%d after onPostSendCTS(),wait data timeout",id);
                    onPostCommunication(true, true);
                }
            }
        }, DataFrame.getDataTimeOut());
        assertCurrentStatus(currentStatus = Status.SENDING_CTS);
        currentStatus = Status.WAITING_DATA;
    }

    private void onPreSendSIFSAndDATA() {
        assertCurrentMode(READ_MODE);
        setWriteMode();
        logger.log("%d onPreSendSIFSAndDATA()",id);
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
        logger.log("%d onPreSendData()",id);
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
        assertCurrentMode(WRITE_MODE);
        setReadMode();
        logger.log("%d onPostSendDATA()",id);
        //设置Data的Timeout,过时当碰撞处理
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (currentStatus == Status.WAITING_ACK) {
                    logger.log("%d after onPostSendDATA(),wait ack timeout",id);
                    onPostCommunication(true , true);
                }
            }
        }, AckFrame.getAckTimeOut());
        assertCurrentStatus(currentStatus = Status.SENDING_DATA);
        currentStatus = Status.WAITING_ACK;
    }

    private void onPreSendSIFSAndACK(final AckFrame frame) {
        assertCurrentMode(READ_MODE);
        setWriteMode();
        logger.log("%d onPreSendSIFSAndACK()",id);
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
        logger.log("%d onPreSendAck()",id);
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
        assertCurrentMode(WRITE_MODE);
        setReadMode();
        logger.log("%d onPostSendACK()",id);
        onPostCommunication(false, false);
    }
    //</editor-fold>

    //<editor-fold desc="接受数据时间点函数">

    /**
     * 开始接受rts frame
     * @param frame 即将被接受的frame
     */
    void onPreRecvRTS(final RtsFrame frame) {
        if(currentCommunicationTarget == defaultCommunicationTarget){
            if(frame.getTargetId() == id) {
                logger.log("%d onPreRecvRTS()",id);
                currentCommunicationTarget = frame.getSrcId();
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        onPostRecvRTS(frame);
                    }
                }, frame.getTransmitDuration());
                assertCurrentStatus(Status.IDLE);
                currentStatus = Status.RECEIVING_RTS;
            } else {
                //如果对方的通信目标不是自己,则设置NAV
                NAVING = true;
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        NAVING = false;
                        scheduleDIFS(false);
                    }
                },frame.getNavDuration());
            }
        } else {
            logger.log("%d receive a non collision rts ," +
                    "but already in a communication process,just ignore it",id);
        }
    }

    /**
     * {@link #onPreSendSIFSAndCTS(RtsFrame)} 与这个方法是在同一时间点,直接调用这个方法即可
     */
    private void onPostRecvRTS(RtsFrame frame) {
        if (!frame.collision()) {
            logger.log("%d onPostRecvRTS()",id);
            onPreSendSIFSAndCTS(frame);
        }
    }

    void onPreRecvCTS(final CtsFrame frame) {
        if(frame.getSrcId() == currentCommunicationTarget){
            logger.log("%d onPreRecvCTS()",id);
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvCTS(frame);
                }
            }, frame.getTransmitDuration());
            assertCurrentStatus(Status.WAITING_CTS);
            currentStatus = Status.RECEIVING_CTS;
        } else {
            logger.log("%d receive a non collision cts ,but already" +
                    " in a communication process,just ignore it",id);
        }

    }

    /**
     * {@link Stateful#onPreSendSIFSAndDATA()}
     */
    private void onPostRecvCTS(CtsFrame frame) {
        if (!frame.collision()) {
            logger.log("%d onPostRecvCTS()",id);
            onPreSendSIFSAndDATA();
        }
    }

    void onPreRecvData(final DataFrame dataFrame) {
        if(dataFrame.getSrcId() == currentCommunicationTarget) {
            logger.log("%d onPreRecvData()",id);
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvData(dataFrame);
                }
            }, dataFrame.getTransmitDuration());
            assertCurrentStatus(Status.WAITING_DATA);
            currentStatus = Status.RECEIVING_DATA;
        } else {
            logger.log("%d receive a non collision data ," +
                    "but already in a communication process,just ignore it",id);
        }
    }

    /**
     * {@link #onPreSendSIFSAndACK(AckFrame)}
     */
    private void onPostRecvData(DataFrame frame) {
        if (!frame.collision()) {
            logger.log("%d onPostRecvData()",id);
            onPreSendSIFSAndACK(frame.generateAckFrame());
        }
    }

    void onPreRecvACK(final AckFrame frame) {
        if(frame.getSrcId() == currentCommunicationTarget) {
            logger.log("%d onPreRecvACK()",id);
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    onPostRecvACK(frame);
                }
            }, frame.getTransmitDuration());
            assertCurrentStatus(Status.WAITING_ACK);
            currentStatus = Status.RECEIVING_ACK;
        } else {
            logger.log("%d receive a non collision ack ," +
                    "but already in a communication process,just ignore it",id);
        }
    }

    /**
     * //表明发送成功了
     *
     * @param frame
     */
    void onPostRecvACK(AckFrame frame){
        logger.log("%d onPostRecvACK()",id);
    }

    //</editor-fold>

}
