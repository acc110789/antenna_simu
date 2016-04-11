package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.*;
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
public abstract class Stateful {
    // TODO: 16/4/10 需要将TimeController中的时间给成double类型
    // TODO: 16/4/10 暂时没有考虑两件事情在同一时间点发生的概率是0
    private static final int READ_MODE = 1;
    private static final int WRITE_MODE = 2;

    //需要注意的是一旦一个Station进入了写(发送)模式之后,
    //这个Station是不能进行读(接受)操作的,或者说即使Meduim
    //通知我有一个Frame,我不会对这个Frame做出任何的相应
    private int currentMode = READ_MODE;

    private Status currentStatus = Status.IDLE;

    private enum Status {
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
        this.currentMode = READ_MODE;
        // TODO: 16/4/10 可以监听frame并且主动去问Medium,有没有正在发给我的Frame,如果有请立刻给我
    }

    protected boolean isReadMode() {
        return currentMode == READ_MODE;
    }

    protected boolean isWriteMode() {
        return currentMode == WRITE_MODE;
    }

    private void setWriteMode() {
        this.currentMode = WRITE_MODE;
    }

    /**
     * 遭受到了碰撞
     */
    public abstract void onCollision();

    public abstract void onPostDIFS();

    public abstract void onPostSLOT();

    //<editor-fold desc="发送数据时间点函数">

    /**
     * 这个时刻表示是RTS开始的那一刻,立刻进入写模式
     *
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
        currentStatus = Status.SENDING_RTS;
    }

    private void onPostSendRTS() {
        setReadMode();
        //设置RTS超时时间,如果超时,则直接判定为碰撞
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onCollision();
            }
        }, RtsFrame.getRtsTimeOut());
        currentStatus = Status.WAITING_CTS;
    }

    /**
     * 这个时刻表示是CTS之前的的SIFS开始的时刻,在这个时刻,
     * Station进入了写模式
     */
    protected void onPreSendSIFSAndCTS(final Frame frame) {
        setWriteMode();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendCTS(frame.generateCtsFrame());
            }
        }, Config.getInstance().getSifs());
        currentStatus = Status.SENDING_SIFS_CTS;
    }

    private void onPreSendCTS(CtsFrame frame) {
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendCTS();
            }
        }, frame.getTransmitDuration());
        currentStatus = Status.SENDING_CTS;
    }

    /**
     * CTS 发送完毕
     */
    private void onPostSendCTS() {
        setReadMode();
        //设置CTS超时,如果超时,直接判定为碰撞
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                // TODO: 16/4/10 CTS超时之后干什么呢?
            }
        }, CtsFrame.getCtsTimeOut());
        currentStatus = Status.WAITING_DATA;
    }

    protected void onPreSendSIFSAndDATA(final DataFrame frame) {
        setWriteMode();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendData(frame);
            }
        }, Config.getInstance().getSifs());
        currentStatus = Status.SENDING_SIFS_DATA;
    }

    private void onPreSendData(DataFrame dataFrame) {
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendDATA();
            }
        }, dataFrame.getTransmitDuration());
        currentStatus = Status.SENDING_DATA;
    }

    private void onPostSendDATA() {
        setReadMode();
        //设置Data的Timeout,过时当碰撞处理
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onCollision();
            }
        }, AckFrame.getAckTimeOut());
        currentStatus = Status.WAITING_ACK;
    }

    protected void onPreSendSIFSAndACK(final AckFrame frame) {
        setWriteMode();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendAck(frame);
            }
        }, Config.getInstance().getSifs());
        currentStatus = Status.SENDING_SIFS_ACK;
    }

    private void onPreSendAck(AckFrame frame) {
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendACK();
            }
        }, frame.getTransmitDuration());
        Medium.getInstance().putFrame(frame);
        currentStatus = Status.SENDING_ACK;
    }

    private void onPostSendACK() {
        setReadMode();
        currentStatus = Status.IDLE;
        // TODO: 16/4/10 对DIFS的监控
    }
    //</editor-fold>

    private boolean canRecv(){
        // TODO: 16/4/11 思考如何才能保证可以接受
        return currentMode == READ_MODE;
    }

    //<editor-fold desc="接受数据时间点函数">
    /**开始接受rts frame
     * @param frame 即将被接受的frame
     */
    protected void onPreRecvRTS(final RtsFrame frame){
        if(canRecv()) {
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    if(!frame.collision()) {
                        onPostRecvRTS(frame);
                    }
                }
            }, frame.getTransmitDuration());
            currentStatus = Status.RECEIVING_RTS;
        }
    }

    /**
     * {{@link Stateful#onPreSendSIFSAndCTS(Frame)}} 与这个方法是在同一时间点,直接调用这个方法即可
     */
    private void onPostRecvRTS(RtsFrame frame){
        onPreSendSIFSAndCTS(frame);
    }
    //</editor-fold>

}
