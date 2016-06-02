package me.zhangxl.antenna.infrastructure.frame_process;

import me.zhangxl.antenna.cool.DifsCool;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;

import static me.zhangxl.antenna.infrastructure.base.BaseRole.defaultCommunicationTarget;

/**
 * 收到Ack之后的处理逻辑
 * Created by zhangxiaolong on 16/6/1.
 */
class AckProcessor extends AbstractProcessor {
    AckProcessor(Station station) {
        super(station);
    }

    @Override
    public void process(Frame frame) {
        logger.debug("%d onPostRecvACK()", station.getId());
        if(station.getCommunicationTarget() != frame.getSrcId()){
            logger.info("this ack is from a unknown peer,nav is 0,will switch to cooling");
            //开始difs冷却
            station.setCommunicationTarget(defaultCommunicationTarget);
            // TODO: 16/6/2 这里如果是sender的话要将backoff加倍
            new DifsCool(station).cool();
        } else {
            //桢是来自正确的节点
            assert station.getCurrentStatus() == Status.WAITING_ACK;
            assert frame.getTargetId() == station.getId();
            //说明本节点发送数据成功
            station.setCommunicationTarget(defaultCommunicationTarget);
            station.onSuccess();
            new DifsCool(station).cool();
        }
    }
}
