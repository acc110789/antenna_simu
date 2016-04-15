package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.RtsFrame;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
interface SenderRole extends Role{

    /**
     * 这个时刻表示是RTS开始的那一刻,立刻进入写模式
     *
     * @param frame 待发送的RTS
     */
    void onPreSendRTS(RtsFrame frame);

    void onPostSendRTS();

    void onPreSendSIFSAndDATA();

    void onPreSendData(DataFrame dataFrame);

    void onPostSendDATA();

    void onPreRecvCTS(final CtsFrame frame);

    void onPostRecvCTS(CtsFrame frame);

    void onPreRecvACK(final AckFrame frame);

    /**
     * 表明发送成功了
     * @param frame
     */
    void onPostRecvACK(AckFrame frame);
}
