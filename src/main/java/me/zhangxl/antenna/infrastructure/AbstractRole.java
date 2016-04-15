package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.Logger;

import static me.zhangxl.antenna.infrastructure.BaseRole.Mode.READ_MODE;
import static me.zhangxl.antenna.infrastructure.BaseRole.Mode.WRITE_MODE;

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

    // TODO: 16/4/10 暂时没有考虑两件事情在同一时间点发生的概率是0
    private static Logger logger = new Logger(AbstractRole.class);
    private final int id;
    private boolean NAVING = false;

    //需要注意的是一旦一个Station进入了写(发送)模式之后,
    //这个Station是不能进行读(接受)操作的,或者说即使Meduim
    //通知我有一个Frame,我不会对这个Frame做出任何的相应
    private Mode currentMode = READ_MODE;
    private Status currentStatus = Status.IDLE;

    /**
     * 节点的当前通信对象
     */
    private int currentCommunicationTarget = defaultCommunicationTarget;


    AbstractRole(int id){
        this.id = id;
    }

    @Override
    public void setReadMode() {
        if (this.currentMode != WRITE_MODE) {
            throw new IllegalStateException("interesting, already in read mode");
        }
        this.currentMode = READ_MODE;
        Medium.getInstance().notify((Station) this);
    }

    @Override
    public void setWriteMode() {
        this.currentMode = WRITE_MODE;
    }

    abstract void backOffDueToTimeout();

    abstract void scheduleDIFS(boolean Immediate);

    @Override
    public void onPostCommunication(boolean success, boolean timeout){
        assert getCurrentStatus() != Status.IDLE;
        if(!success && getCurrentStatus().isSender()){
            backOffDueToTimeout();
        }
        setCurrentStatus(Status.IDLE);
        setCommunicationTarget(defaultCommunicationTarget);
        scheduleDIFS(timeout);
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
    public Mode getCurrentMode() {
        return currentMode;
    }

    @Override
    public Status getCurrentStatus() {
        return this.currentStatus;
    }

    @Override
    public void setCurrentStatus(Status status) {
        this.currentStatus = status;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setNAV() {
        this.NAVING = true;
    }

    @Override
    public void unsetNAV() {
        this.NAVING = false;
    }

    @Override
    public boolean inNAV() {
        return this.NAVING;
    }
}
