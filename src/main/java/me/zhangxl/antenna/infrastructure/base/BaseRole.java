package me.zhangxl.antenna.infrastructure.base;

import me.zhangxl.antenna.frame.DataFrame;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
public interface BaseRole extends Locatable {

    int defaultCommunicationTarget = -1;

    DataFrame getDataFrameToSend();

    int getId();

    void setCommunicationTarget(int id);

    int getCommunicationTarget();
}
