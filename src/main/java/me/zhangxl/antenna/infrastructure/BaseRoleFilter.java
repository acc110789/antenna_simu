package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.medium.Medium;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
class BaseRoleFilter implements BaseRole {

    private final BaseRole mBaseRole;

    BaseRoleFilter(BaseRole baseRole){
        this.mBaseRole = baseRole;
    }

    @Override
    public void setReadMode() {
        mBaseRole.setReadMode();
    }

    @Override
    public void setWriteMode() {
        mBaseRole.setWriteMode();
    }

    @Override
    public Status getCurrentStatus() {
        return mBaseRole.getCurrentStatus();
    }

    @Override
    public Mode getCurrentMode() {
        return mBaseRole.getCurrentMode();
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
    public void onPostTimeOut() {
        mBaseRole.onPostTimeOut();
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
}
