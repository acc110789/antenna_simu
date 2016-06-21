package me.zhangxl.antenna.infrastructure.station.receive_logic.onpair;

import me.zhangxl.antenna.frame.PairFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.frame_process.PairProcessor;

/**
 * station 接收到一个PairFrame,
 * 这个PairFrame中的srcId是本station。
 * Created by zhangxiaolong on 16/5/15.
 */
public class SrcMatchPairAction extends AbstractPairAction {

    private final PairFrame frame;
    public SrcMatchPairAction(Station station, PairFrame frame) {
        super(station);
        this.frame = frame;
    }

    @Override
    public void action() {
        station.setAcceptFre(frame.getChannel());
        station.setCommunicationTarget(frame.getTargetId());
        new PairProcessor(station).process(frame);
    }
}
