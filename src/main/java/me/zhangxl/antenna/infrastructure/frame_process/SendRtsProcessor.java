package me.zhangxl.antenna.infrastructure.frame_process;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.medium.Medium;

import static me.zhangxl.antenna.infrastructure.frame_process.AbstractProcessor.logger;

/**
 * 发送Rts的逻辑部分
 * Created by zhangxiaolong on 16/6/1.
 */
public class SendRtsProcessor implements Processor {
    private final Station station;

    public SendRtsProcessor(Station station) {
        this.station = station;
    }

    @Override
    public void process(Frame frame) {
        assert frame instanceof RtsFrame;
        logger.debug("%d onPreSendRTS()  channel:%d", station.getId(),frame.getFre());
        assert station.getCurrentStatus() == Status.SLOTING;
        station.setCurrentStatus(Status.SENDING_RTS);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendRTS();
            }
        }, frame.getTransmitDuration(), TimeTask.SEND);
        station.setCommunicationTarget(frame.getTargetId());
        sendFrame(frame);
    }

    private void onPostSendRTS() {
        logger.debug("%d onPostSendRTS()",station.getId());
        assert station.getCurrentStatus() == Status.SENDING_RTS;
        station.setCurrentStatus(Status.WAITING_BACK_OFF);
        //new WaitCtsTimeOut(station).await();
    }

    private void sendFrame(Frame frame) {
        try {
            Medium.getInstance().putFrame(station, (Frame) frame.clone());
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

}
