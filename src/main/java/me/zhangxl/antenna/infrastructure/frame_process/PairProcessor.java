package me.zhangxl.antenna.infrastructure.frame_process;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.timeout.WaitAckTimeOut;
import me.zhangxl.antenna.util.Config;

/**
 * 发送方收到对应的Pcp节点回复的PairFrame(即得到允许可以开始发送DataFrame了)
 * Created by zhangxiaolong on 16/6/21.
 */
public class PairProcessor extends AbstractProcessor {

    public PairProcessor(Station station) {
        super(station);
    }

    @Override
    public void processInner(Frame frame) {
        logger.debug("%d onPostRecvPair()", station.getId());
        assert station.getId() == frame.getSrcId() && station.getCommunicationTarget() == frame.getTargetId();
        onPreSendSIFSAndDATA();
    }

    @Override
    Status getRightStatus() {
        return Status.RECEIVING_PAIR_FRAME;
    }

    private void onPreSendSIFSAndDATA() {
        logger.debug("%d onPreSendSIFSAndDATA()", station.getId());
        assert station.getCurrentStatus() == getRightStatus();
        station.setCurrentStatus(Status.SENDING_DATA);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendData(station.getDataFrameToSend());
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
