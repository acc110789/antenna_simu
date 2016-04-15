package me.zhangxl.antenna.infrastructure;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
interface ReceiveBaseRole extends BaseRole {

    void setNAV();

    void unsetNAV();

    boolean inNAV();

}
