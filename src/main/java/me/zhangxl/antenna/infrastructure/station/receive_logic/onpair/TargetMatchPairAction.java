package me.zhangxl.antenna.infrastructure.station.receive_logic.onpair;

import me.zhangxl.antenna.frame.PairFrame;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.timeout.WaitDataTimeOut;

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
        station.setAcceptFre(frame.getChannel());
        station.setCommunicationTarget(frame.getSrcId());
        new WaitDataTimeOut(station).await();
    }
}
