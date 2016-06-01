package me.zhangxl.antenna.frame_process;

import me.zhangxl.antenna.frame.CtsFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.util.Config;

import static me.zhangxl.antenna.infrastructure.base.BaseRole.defaultCommunicationTarget;


/**
 * 收到Rts之后的处理逻辑
 * Created by zhangxiaolong on 16/6/1.
 */
class RtsProcessor extends AbstractProcessor{
    RtsProcessor(Station station) {
        super(station);
    }

    @Override
    public void process(Frame frame) {
        logger.debug("%d onPostRecvRTS()", station.getId());
        if(station.getCurrentStatus() == Status.IDLE_RECEIVING){
            assert station.getCommunicationTarget() == defaultCommunicationTarget;
            if(frame.getTargetId() == station.getId()){
                //的确是发给本Station的,则开启会话
                station.setCommunicationTarget(frame.getSrcId());
                station.setCurrentStatus(Status.RECEIVING_RTS);
                onPreSendSIFSAndCTS((RtsFrame) frame);
            } else {
                //不是发给本Station的,这种情况下应当设置NAV向量
                station.setCurrentStatus(Status.NAV);
                logger.info("%d set NAV",station.getId());
                TimeController.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        logger.info("%d NAV finish",station.getId());
                        station.endCommunication(false,false);
                    }
                },frame.getNavDuration(), TimeTask.RECEIVE);
            }
        } else {
            logger.debug("%d ignore RTS because currentStatus is :%s",
                    station.getId(),station.getCurrentStatus().toString());
        }
    }

    private void onPreSendSIFSAndCTS(final RtsFrame frame) {
        logger.debug("%d onPreSendSIFSAndCTS()", station.getId());
        assert station.getCurrentStatus() == Status.RECEIVING_RTS;
        station.setCurrentStatus(Status.SENDING_SIFS_CTS);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPreSendCTS(frame.generateCtsFrame());
            }
        },Config.getInstance().getSifs(),TimeTask.SEND);
    }

    private void onPreSendCTS(CtsFrame frame) {
        logger.debug("%d onPreSendCTS()", station.getId());
        assert station.getCurrentStatus() == Status.SENDING_SIFS_CTS;
        station.setCurrentStatus(Status.SENDING_CTS);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                onPostSendCTS();
            }
        },frame.getTransmitDuration(),TimeTask.SEND);
        sendFrame(frame);
    }

    private void onPostSendCTS() {
        logger.debug("%d onPostSendCTS()", station.getId());
        assert station.getCurrentStatus() == Status.SENDING_CTS;
        station.setCurrentStatus(Status.WAITING_DATA);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (station.getCurrentStatus() == Status.WAITING_DATA) {
                    logger.info("station :%d after onPostSendCTS(),wait data timeout", station.getId());
                    station.endCommunication(false,true);
                }
            }
        },DataFrame.getDataTimeOut(),TimeTask.DATA_TIMEOUT);
    }
}
