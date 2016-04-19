package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.Logger;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
class BaseRoleFilter implements BaseRole {

    private static final Logger receiverLogger = new Logger(Receiver.class);
    private static final Logger senderLogger = new Logger(Sender.class);

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
    public void onPostCommunication(boolean success, boolean fail) {
        mBaseRole.onPostCommunication(success,fail);
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
        frame.setStartTimeNow();
        Medium.getInstance().putFrame((Station) mBaseRole,frame);
    }

    void onPostRecvMethod(String info, Frame frame,
                          Status lastStatus, Status currentStatus, Runnable nextAction){
        receiverLogger.log(info);
        if(getCurrentStatus() == lastStatus){
            if(getCommunicationTarget() != frame.getSrcId()){
                receiverLogger.log("%d this frame is not from its' communication target");
            } else if(getId() != frame.getTargetId()){
                receiverLogger.log("%d this frame from %d is not sent to %d",frame.getSrcId(),getId());
            } else {
                setCurrentStatus(currentStatus);
                if(nextAction != null){
                    nextAction.run();
                }
            }
        } else {
            receiverLogger.log("%d receive a unexpected frame,ignore this frame :%s :%s",
                    getCurrentStatus().toString(),frame.getClass().getSimpleName());
        }
    }

    void onSendMethod(String info, Status lastStatus,
                      Status currentStatus, Runnable toPost, double postTime){
        senderLogger.log(info);
        assert getCurrentStatus() == lastStatus;
        setCurrentStatus(currentStatus);
        TimeController.getInstance().post(toPost,postTime);
    }
}
