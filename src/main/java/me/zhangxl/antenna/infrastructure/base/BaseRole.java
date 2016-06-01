package me.zhangxl.antenna.infrastructure.base;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.ReceiverExpandRole;
import me.zhangxl.antenna.infrastructure.SenderExpandRole;
import me.zhangxl.antenna.infrastructure.Station;

/**
 * Created by zhangxiaolong on 16/4/15.
 */
public interface BaseRole extends Locatable {

    int defaultCommunicationTarget = -1;

    DataFrame getDataToSend();

    int getId();

    /**
     * 表明通信已经完毕,以下情况会使得通信完毕
     * (1)通信成功,则通信双方应该reset通信目标
     *      对于发送方来说 {@link SenderExpandRole#onPostRecvACK(AckFrame)}
     *      对于接受方来说 {@link ReceiverExpandRole#onPostSendACK()}
     * (2)通信失败,对于一方来说是遭受到了碰撞,对于另一方来说是超时没有收到期待的frame
     *      碰撞发生的地点 {@link Station#(Frame)}
     *      超时的地方 {@link SenderExpandRole#onPostSendRTS()}
     *                {@link ReceiverExpandRole#onPostSendCTS()}
     *                {@link SenderExpandRole#onPostSendDATA()}
     * (3)NAV结束
     * 通信完毕之后有几件事情是需要注意的:
     * (1)如果通信失败,则对于发送方来说需要将碰撞次数加一,扩大碰撞窗口.接收方则不受影响
     * (2)如果是 超时性质的通信失败,则要马上执行{@link Station#onPostDIFS()}
     * 否则需要等DIFS之后才能执行{@link Station#onPostDIFS()}
     * @param success
     * @param fail
     */
    void endCommunication(boolean success, boolean fail);

    void setCommunicationTarget(int id);

    int getCommunicationTarget();
}
