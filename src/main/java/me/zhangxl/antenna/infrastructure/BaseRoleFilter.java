package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import org.apache.logging.log4j.Logger;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
class BaseRoleFilter implements BaseRole {

    private final BaseRole mBaseRole;

    BaseRoleFilter(BaseRole baseRole){
        this.mBaseRole = baseRole;
    }

    @Override
    public Status getCurrentStatus() {
        return mBaseRole.getCurrentStatus();
    }

    @Override
    public void setCurrentStatus(Status status) {
        mBaseRole.setCurrentStatus(status);
    }

    @Override
    public DataFrame getDataToSend() {
        return mBaseRole.getDataToSend();
    }

    @Override
    public int getId() {
        return mBaseRole.getId();
    }

    @Override
    public void endCommunication(boolean success, boolean fail) {
        mBaseRole.endCommunication(success,fail);
    }

    @Override
    public int getCommunicationTarget() {
        return mBaseRole.getCommunicationTarget();
    }

    @Override
    public void setCommunicationTarget(int id) {
        mBaseRole.setCommunicationTarget(id);
    }

    void sendFrame(Frame frame){
        try {
            Medium.getInstance().putFrame((Station) mBaseRole, (Frame) frame.clone());
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    void onPostRecvMethod(Logger receiverLogger, String info, Frame frame,
                          Status lastStatus, Status currentStatus, Runnable nextAction){
        receiverLogger.debug(info);
        if(getCurrentStatus() == lastStatus){
            if(getCommunicationTarget() != frame.getSrcId()){
                receiverLogger.debug("%d this frame is not from its' communication target :%d",
                        getId(),getCommunicationTarget());
            } else if(getId() != frame.getTargetId()){
                receiverLogger.debug("%d this frame from %d is not sent to %d",frame.getSrcId(),getId());
            } else {
                setCurrentStatus(currentStatus);
                if(nextAction != null){
                    nextAction.run();
                }
            }
        } else {
            receiverLogger.debug("%d receive a unexpected frame,ignore this frame :%s :%s",
                    getId(),getCurrentStatus().toString(),frame.getClass().getSimpleName());
        }
    }

    void onSendMethod(Logger senderLogger, String info, Status lastStatus,
                      Status currentStatus, Runnable toPost, double postTime){
        onSendMethod(senderLogger, info, lastStatus,
                currentStatus, toPost, postTime, TimeTask.COMMON_PRIORITY);
    }

    void onSendMethod(Logger senderLogger, String info, Status lastStatus,
                      Status currentStatus, Runnable toPost, double postTime, int priority){
        senderLogger.debug(info);
        assert getCurrentStatus() == lastStatus;
        setCurrentStatus(currentStatus);
        TimeController.getInstance().post(toPost,postTime,priority);
    }
}
