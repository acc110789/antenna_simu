package me.zhangxl.antenna.infrastructure.frame_process;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.ChannelManager;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;

/**
 * sender收到Ack之后的处理逻辑
 * Created by zhangxiaolong on 16/6/1.
 */
public class AckProcessor extends AbstractProcessor {
    public AckProcessor(Station station) {
        super(station);
    }

    @Override
    public void processInner(Frame frame) {
        logger.debug("%d onPostRecvACK()", station.getId());
        if(needNavById(frame)){
            //这里ack的nav值应该是0,所以直接进入difs
            logger.error("this ack is from a unknown peer,nav is 0,will switch to cooling");
            station.onFail();
            //开始difs冷却
            station.setAcceptFre(ChannelManager.getInstance().getPcpChannel());
            station.setCurrentStatus(Status.WAITING_BACK_OFF);
        } else {
            //桢是来自正确的节点
            assert station.getCurrentStatus() == Status.RECEIVING_ACK;
            assert frame.getTargetId() == station.getId();
            //说明本节点发送数据成功
            station.onSuccess();
            station.setAcceptFre(ChannelManager.getInstance().getPcpChannel());
            station.setCurrentStatus(Status.WAITING_BACK_OFF);
        }
    }

    @Override
    Status getRightStatus() {
        return Status.RECEIVING_ACK;
    }
}
