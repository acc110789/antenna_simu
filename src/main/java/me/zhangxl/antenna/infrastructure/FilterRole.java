package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.DataFrame;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
public class FilterRole implements Role {

    static int defaultCommunicationTarget = -1;

    /**
     * 节点的当前通信对象
     */
    int currentCommunicationTarget = defaultCommunicationTarget;

    private final Role mRole;

    public FilterRole(Role role){
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
    public void setCurrentStatus(Status status) {
        mRole.setCurrentStatus(status);
    }

    @Override
    public Status getCurrentStatus() {
        return mRole.getCurrentStatus();
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
}
