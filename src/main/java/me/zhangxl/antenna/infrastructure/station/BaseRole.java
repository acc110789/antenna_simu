package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.DataFrame;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
public interface BaseRole extends Stateful{

    int defaultCommunicationTarget = -1;

    enum Mode{
        READ_MODE,
        WRITE_MODE
    }

    DataFrame getDataToSend();

    int getId();

    void setCommunicationTarget(int id);

    int getCommunicationTarget();
}
