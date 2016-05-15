package me.zhangxl.antenna.infrastructure.station.receive_pair;

import me.zhangxl.antenna.frame.PairFrame;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * station 接收到一个PairFrame,
 * 这个PairFrame中的srcId是本station。
 * Created by zhangxiaolong on 16/5/15.
 */
public class SrcMatchPairAction extends AbstractPairAction {

    private final PairFrame frame;
    public SrcMatchPairAction(Station station,PairFrame frame) {
        super(station);
        this.frame = frame;
    }

    @Override
    public void action() {
        station.mFreFilter.setFre(frame.getChannel());
        station.setCommunicationTarget(frame.getTargetId());
        station.mSender.onPreSendSIFSAndDATA();
    }
}
