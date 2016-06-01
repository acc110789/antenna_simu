package me.zhangxl.antenna.frame_process;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.util.Config;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
class CtsProcessor extends AbstractProcessor {
    CtsProcessor(Station station) {
        super(station);
    }

    @Override
    public void process(Frame frame) {
        logger.debug("%d onPostRecvCTS()", station.getId());
        if (station.getCurrentStatus() == Status.WAITING_CTS) {
            if (station.getCommunicationTarget() != frame.getSrcId()) {
                logger.debug("%d this frame is not from its' communication target :%d",
                        station.getId(), station.getCommunicationTarget());
            } else if (station.getId() != frame.getTargetId()) {
                logger.debug("%d this frame from %d is not sent to %d", frame.getSrcId(), station.getId());
            } else {
                station.setCurrentStatus(Status.RECEIVING_CTS);
                onPreSendSIFSAndDATA();
            }
        } else {
            logger.debug("%d receive a unexpected frame,ignore this frame :%s :%s",
                    station.getId(), station.getCurrentStatus().toString(),
                    frame.getClass().getSimpleName());
        }
    }

    private void onPreSendSIFSAndDATA() {
        logger.debug("%d onPreSendSIFSAndDATA()", station.getId());
        assert station.getCurrentStatus() == Status.RECEIVING_CTS;
        station.setCurrentStatus(Status.SENDING_SIFS_DATA);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendData(station.getDataToSend());
            }
        }, Config.getInstance().getSifs(), TimeTask.SEND);
    }

    private void onPreSendData(DataFrame dataFrame) {
        logger.debug("%d onPreSendData()", station.getId());
        assert station.getCurrentStatus() == Status.SENDING_SIFS_DATA;
        station.setCurrentStatus(Status.SENDING_DATA);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendDATA();
            }
        }, dataFrame.getTransmitDuration(), TimeTask.SEND);
        sendFrame(dataFrame);
    }

    private void onPostSendDATA() {
        logger.debug("%d onPostSendDATA()", station.getId());
        assert station.getCurrentStatus() == Status.SENDING_DATA;
        station.setCurrentStatus(Status.WAITING_ACK);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (station.getCurrentStatus() == Stateful.Status.WAITING_ACK) {
                    logger.info("%d after onPostSendDATA(),wait ack timeout", station.getId());
                    station.endCommunication(false, true);
                }
            }
        }, AckFrame.getAckTimeOut(), TimeTask.ACK_TIMEOUT);
    }
}
