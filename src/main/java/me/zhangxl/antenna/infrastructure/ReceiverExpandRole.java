package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
interface ReceiverExpandRole extends ReceiveBaseRole {

    /**
     * 这个时刻表示是CTS之前的的SIFS开始的时刻,在这个时刻,
     * Station进入了写模式
     */
    void onPreSendSIFSAndCTS(final RtsFrame frame);

    /**
     * 刚刚开始发送CTS的那个时刻
     * @param frame 待发送的CTS
     */
    void onPreSendCTS(CtsFrame frame);

    /**
     * CTS 刚刚发送完毕的那个时刻
     */
    void onPostSendCTS();

    void onPreSendSIFSAndACK(final AckFrame frame);

    void onPreSendAck(AckFrame frame);

    void onPostSendACK();

    /**
     * {@link #onPreSendSIFSAndCTS(RtsFrame)} 与这个方法是在同一时间点,直接调用这个方法即可
     */
    void onPostRecvRTS(RtsFrame frame);

    void onPostRecvData(DataFrame frame);
}
