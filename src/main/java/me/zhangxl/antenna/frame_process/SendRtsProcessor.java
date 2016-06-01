package me.zhangxl.antenna.frame_process;

import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;

/**
 * 发送Rts的逻辑部分
 * Created by zhangxiaolong on 16/6/1.
 */
public class SendRtsProcessor extends AbstractProcessor {

    public SendRtsProcessor(Station station) {
        super(station);
    }

    @Override
    public void process(Frame frame) {
        assert frame instanceof RtsFrame;
        logger.debug("%d onPreSendRTS()", station.getId());
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
        station.setCurrentStatus(Status.WAITING_CTS);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (station.getCurrentStatus() == Status.WAITING_CTS) {
                    logger.info("%d after onPostSendRTS() wait CTS timeout",station.getId());
                    station.endCommunication(false,true);
                }
            }
        }, CtsFrame.getCtsTimeOut(), TimeTask.CTS_TIMEOUT);
    }
}
