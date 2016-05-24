package me.zhangxl.antenna.infrastructure.station.receive_pair;

import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.infrastructure.station.BaseRole;
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
        station.setCommunicationTarget(frame.getSrcId());
        station.setCurrentStatus(BaseRole.Status.WAITING_DATA);
        new ReceiverDataTimeOutWaiter(station).await();
    }
}
