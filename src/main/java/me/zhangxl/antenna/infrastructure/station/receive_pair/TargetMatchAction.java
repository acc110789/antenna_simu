package me.zhangxl.antenna.infrastructure.station.receive_pair;

import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.infrastructure.station.Stateful;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.station.wait.ReceiverDataTimeOutWaiter;

/**
 * PtsFrame 中的targetId是station
 * Created by zhangxiaolong on 16/5/15.
 */
public class TargetMatchAction extends AbstractPairAction {

    private final PtsFrame frame;

    public TargetMatchAction(Station station, PtsFrame frame) {
        super(station);
        this.frame = frame;
    }

    @Override
    public void action() {
        logger.info("%d target received PtsFrame",station.getId());
        station.setCommunicationTarget(frame.getSrcId());
        station.setFocusSector(frame.getSrcId());
        logger.info("%d 将方向对准 %d",station.getId(),frame.getSrcId());
        station.setCurrentStatus(Stateful.Status.WAITING_DATA);
        new ReceiverDataTimeOutWaiter(station).await();
    }
}
