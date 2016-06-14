package me.zhangxl.antenna.infrastructure.base;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
public interface BaseRole extends Locatable {

    int defaultCommunicationTarget = -1;

    void setCommunicationTarget(int id);

    int getCommunicationTarget();
}
