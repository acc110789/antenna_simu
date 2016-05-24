package me.zhangxl.antenna.infrastructure.station.receive_pair;

import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * station 接收到一个PairFrame,
 * 这个PairFrame中的srcId是本station。
 * Created by zhangxiaolong on 16/5/15.
 */
public class SrcMatchAction extends AbstractPairAction {

    private final PtsFrame frame;
    public SrcMatchAction(Station station, PtsFrame frame) {
        super(station);
        this.frame = frame;
    }

    @Override
    public void action() {
        station.setCommunicationTarget(frame.getTargetId());
        station.mSender.onPostRecvPTS(frame);
    }
}