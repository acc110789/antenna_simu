package me.zhangxl.antenna.infrastructure.station.receive_pair;

import me.zhangxl.antenna.frame.PairFrame;
import me.zhangxl.antenna.infrastructure.station.BaseRole;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * PairFrame 中的targetId是station
 * Created by zhangxiaolong on 16/5/15.
 */
public class TargetMatchPairAction extends AbstractPairAction {

    private final PairFrame frame;

    public TargetMatchPairAction(Station station, PairFrame frame) {
        super(station);
        this.frame = frame;
    }

    @Override
    public void action() {
        station.mFreFilter.setFre(frame.getChannel());
        station.setCommunicationTarget(frame.getSrcId());
        station.setCurrentStatus(BaseRole.Status.WAITING_DATA_FRAME);
    }
}
