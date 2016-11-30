package me.zhangxl.antenna.infrastructure.frame_process;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.ChannelManager;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.nav.DataNav;
import me.zhangxl.antenna.util.Config;

/**
 * 收到DataFrame之后的处理逻辑
 * Created by zhangxiaolong on 16/6/1.
 */
public class DataProcessor extends AbstractProcessor {
    public DataProcessor(Station station) {
        super(station);
    }

    @Override
    public void processInner(Frame frame) {
        logger.debug("%d onPostRecvData()", station.getId());
        if(needNavById(frame)){
            new DataNav(station).startNav();
        } else {
            station.setCurrentStatus(Status.RECEIVING_DDATA);
            onPreSendSIFSAndACK(frame.generateAckFrame());
        }
    }

    @Override
    Status getRightStatus() {
        return Status.RECEIVING_DDATA;
    }

    private void onPreSendSIFSAndACK(final AckFrame frame) {
        logger.debug("%d onPreSendSIFSAndACK()", station.getId());
        assert station.getCurrentStatus() == Status.RECEIVING_DDATA;
        station.setCurrentStatus(Status.SENDING_DACK);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendAck(frame);
            }
        }, Config.getSifs(), TimeTask.SEND);
    }

    private void onPreSendAck(AckFrame frame) {
        logger.debug("%d onPreSendAck()", station.getId());
        assert station.getCurrentStatus() == Status.SENDING_DACK;
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendACK();
            }
        }, frame.getTransmitDuration(), TimeTask.SEND);
        sendFrame(frame);
    }

    private void onPostSendACK() {
        logger.debug("%d onPostSendACK()", station.getId());
        assert station.getCurrentStatus() == Status.SENDING_DACK;
        station.setAcceptFre(ChannelManager.getPcpChannel());
        station.setCurrentStatus(Status.WAITING_BOF);
    }
}
