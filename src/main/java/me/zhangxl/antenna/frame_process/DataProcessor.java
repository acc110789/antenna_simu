package me.zhangxl.antenna.frame_process;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.util.Config;

/**
 * 收到DataFrame之后的处理逻辑
 * Created by zhangxiaolong on 16/6/1.
 */
class DataProcessor extends AbstractProcessor {
    DataProcessor(Station station) {
        super(station);
    }

    @Override
    public void process(Frame frame) {
        logger.debug("%d onPostRecvData()", station.getId());
        if (station.getCurrentStatus() == Status.WAITING_DATA) {
            if (station.getCommunicationTarget() != frame.getSrcId()) {
                logger.debug("%d this frame is not from its' communication target :%d",
                        station.getId(), station.getCommunicationTarget());
            } else if (station.getId() != frame.getTargetId()) {
                logger.debug("%d this frame from %d is not sent to %d", frame.getSrcId(), station.getId());
            } else {
                station.setCurrentStatus(Status.RECEIVING_DATA);
                onPreSendSIFSAndACK(frame.generateAckFrame());
            }
        } else {
            logger.debug("%d receive a unexpected frame,ignore this frame :%s :%s",
                    station.getId(), station.getCurrentStatus().toString(),
                    frame.getClass().getSimpleName());
        }
    }

    private void onPreSendSIFSAndACK(final AckFrame frame) {
        logger.debug("%d onPreSendSIFSAndACK()", station.getId());
        assert station.getCurrentStatus() == Status.RECEIVING_DATA;
        station.setCurrentStatus(Status.SENDING_SIFS_ACK);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendAck(frame);
            }
        }, Config.getInstance().getSifs(), TimeTask.SEND);
    }

    private void onPreSendAck(AckFrame frame) {
        logger.debug("%d onPreSendAck()", station.getId());
        assert station.getCurrentStatus() == Status.SENDING_SIFS_ACK;
        station.setCurrentStatus(Status.SENDING_ACK);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendACK();
            }
        }, frame.getTransmitDuration(), TimeTask.SEND);
        sendFrame(frame);
        sendFrame(frame);
    }

    private void onPostSendACK() {
        logger.debug("%d onPostSendACK()", station.getId());
        assert station.getCurrentStatus() == Status.SENDING_ACK;
        station.endCommunication(true, false);
    }
}
