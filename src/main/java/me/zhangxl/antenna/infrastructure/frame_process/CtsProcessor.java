package me.zhangxl.antenna.infrastructure.frame_process;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.timeout.WaitAckTimeOut;
import me.zhangxl.antenna.nav.CtsNav;
import me.zhangxl.antenna.util.Config;

/**
 * 收到CtsFrame之后的处理逻辑
 * Created by zhangxiaolong on 16/6/1.
 */
class CtsProcessor extends AbstractProcessor {
    CtsProcessor(Station station) {
        super(station);
    }

    @Override
    public void processInner(Frame frame) {
        logger.debug("%d onPostRecvCTS()", station.getId());
        if(needNavById(frame)){
            new CtsNav(station).startNav();
        } else {
            onPreSendSIFSAndDATA();
        }
    }

    @Override
    Status getRightStatus() {
        return Status.RECEIVING_CTS;
    }

    private void onPreSendSIFSAndDATA() {
        logger.debug("%d onPreSendSIFSAndDATA()", station.getId());
        assert station.getCurrentStatus() == Status.RECEIVING_CTS;
        station.setCurrentStatus(Status.SENDING_DATA);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendData(station.getDataToSend());
            }
        }, Config.getInstance().getSifs(), TimeTask.SEND);
    }

    private void onPreSendData(DataFrame dataFrame) {
        logger.debug("%d onPreSendData()", station.getId());
        assert station.getCurrentStatus() == Status.SENDING_DATA;
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
        new WaitAckTimeOut(station).await();
    }
}
