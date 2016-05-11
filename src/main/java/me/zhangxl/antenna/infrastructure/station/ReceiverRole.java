package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.infrastructure.base.BaseRole;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
interface ReceiverRole extends BaseRole {
    void onPostRecvData(DataFrame frame);
}
