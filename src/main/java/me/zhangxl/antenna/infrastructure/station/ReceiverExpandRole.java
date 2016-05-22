package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
interface ReceiverExpandRole extends ReceiveBaseRole {

    void onPostRecvRTS(RtsFrame frame);

    void onPreSendAck(AckFrame frame);

    void onPostSendACK();

    void onPostRecvData(DataFrame frame);
}
