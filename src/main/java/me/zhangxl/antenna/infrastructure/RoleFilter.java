package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.medium.Medium;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
class RoleFilter implements Role {

    private final Role mRole;

    RoleFilter(Role role){
        this.mRole = role;
    }

    @Override
    public void setReadMode() {
        mRole.setReadMode();
    }

    @Override
    public void setWriteMode() {
        mRole.setWriteMode();
    }

    @Override
    public Status getCurrentStatus() {
        return mRole.getCurrentStatus();
    }

    @Override
    public void setCurrentStatus(Status status) {
        mRole.setCurrentStatus(status);
    }

    @Override
    public void assertCurrentStatus(Status status) {
        mRole.assertCurrentStatus(status);
    }

    @Override
    public void assertCurrentMode(int mode) {
        mRole.assertCurrentMode(mode);
    }

    @Override
    public DataFrame getDataToSend() {
        return mRole.getDataToSend();
    }

    @Override
    public int getId() {
        return mRole.getId();
    }

    @Override
    public void onPostCommunication(boolean fail, boolean timeout) {
        mRole.onPostCommunication(fail,timeout);
    }

    @Override
    public void backOffDueToTimeout() {
        mRole.backOffDueToTimeout();
    }

    @Override
    public void scheduleDIFS(boolean Immediate) {
        mRole.scheduleDIFS(Immediate);
    }

    @Override
    public void scheduleSLOT() {
        mRole.scheduleSLOT();
    }

    @Override
    public void setNAV() {
        mRole.setNAV();
    }

    @Override
    public void unsetNAV() {
        mRole.unsetNAV();
    }

    @Override
    public boolean inNAV() {
        return mRole.inNAV();
    }

    @Override
    public int getCommunicationTarget() {
        return mRole.getCommunicationTarget();
    }

    @Override
    public void setCommunicationTarget(int id) {
        mRole.setCommunicationTarget(id);
    }

    void sendFrame(Frame frame){
        frame.setStartTimeNow();
        Medium.getInstance().putFrame(mRole,frame);
    }
}
