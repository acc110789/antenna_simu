package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

/**
 * 对于全向天线来说,所有的Statin的状态都是同步的.
 * 但是对于定向天线来说,如果一个DataFrame的发送
 * 过程是成功的,则至少发送方和接收方的状态应该是同步的.
 * 整个发送和接受过程如下:
 * Src:|<--DIFS-->|<-RTS->|              |<SIFS>|<---DATA--->|
 * Det:                   |<SIFS>|<-CTS->|                   |<SIFS>|<-ACK->|
 * Third:                                                                   |<--DIFS-->|<NEWSLOT>|...
 * 上述 DIFS RTS SIFS 等都是一个过程
 * 用pre表示过程的开始的那一刻,用post表示过程结束的那一刻
 *
 * Created by zhangxiaolong on 16/4/15.
 */
abstract class AbstractRole implements ReceiveBaseRole,SendBaseRole {

    private static Logger logger = SimuLoggerManager.getLogger(AbstractRole.class.getSimpleName());
    private final int id;

    //需要注意的是一旦一个Station进入了写(发送)模式之后,
    //这个Station是不能进行读(接受)操作的,或者说即使Meduim
    //通知我有一个Frame,我不会对这个Frame做出任何的相应
    private Status currentStatus = Status.SLOTING;

    /**
     * 节点的当前通信对象
     */
    private int currentCommunicationTarget = defaultCommunicationTarget;


    AbstractRole(int id){
        this.id = id;
    }

    abstract void backOffDueToTimeout();

    abstract void onFinish();

    @Override
    public void endCommunication(boolean success, boolean fail){
        assert getCurrentStatus() != Status.IDLE;
        if(getCurrentStatus().isSender()) {
            //如果发送成功,则统计数据
            if (success) {
                onSendSuccess();
            }
            //如果发送失败,则将backOff窗口加倍
            if (fail) {
                backOffDueToTimeout();
            }
        }
        setCommunicationTarget(defaultCommunicationTarget);
        onFinish();
    }


    @Override
    public int getCommunicationTarget() {
        return this.currentCommunicationTarget;
    }

    @Override
    public void setCommunicationTarget(int id) {
        this.currentCommunicationTarget = id;
    }

    @Override
    public Status getCurrentStatus() {
        return this.currentStatus;
    }

    abstract void onPostDIFS();

    @Override
    public void setCurrentStatus(Status status) {
        Status previous = this.currentStatus;
        this.currentStatus = status;
        if(this.currentStatus.isReadMode()){
            Medium.getInstance().notify((Station)this);
        }
        if(previous.isReadMode() && this.currentStatus.isWriteMode()){
            // TODO: 16/4/19 要将正在接收的frame push到Medium的缓冲区
        }
        if(getCurrentStatus() == Status.IDLE) {
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    TimeController.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            TimeController.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    if(getCurrentStatus() == Status.IDLE){
                                        setCurrentStatus(Status.SLOTING);
                                    }
                                }
                            },0);
                        }
                    },0);
                }
            }, Config.getInstance().getDifs());
        } else if(getCurrentStatus() == Status.SLOTING){
            onPostDIFS();
        }
    }

    @Override
    public int getId() {
        return this.id;
    }
}
