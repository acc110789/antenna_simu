package me.zhangxl.antenna.frame_process;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.util.TimeLogger;

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
        if (station.getCurrentStatus() == Status.WAITING_ACK) {
            if (station.getCommunicationTarget() != frame.getSrcId()) {
                logger.debug("%d this frame is not from its' communication target :%d",
                        station.getId(), station.getCommunicationTarget());
            } else if (station.getId() != frame.getTargetId()) {
                logger.debug("%d this frame from %d is not sent to %d", frame.getSrcId(), station.getId());
            } else {
                station.setCurrentStatus(Status.RECEIVING_ACK);
                if (TimeLogger.DEBUG_STATION) {
                    logger.debug("%d send a data successfully...", station.getId());
                }
                station.endCommunication(true, false);
            }
        } else {
            logger.debug("%d receive a unexpected frame,ignore this frame :%s :%s",
                    station.getId(), station.getCurrentStatus().toString(), frame.getClass().getSimpleName());
        }
    }
}
