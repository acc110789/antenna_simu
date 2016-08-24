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
        logger.info("%d src received PtsFrame",station.getId());
        station.setCommunicationTarget(frame.getTargetId());
        station.setFocusSector(frame.getTargetId());
        logger.info("%d 将方向对准 %d",station.getId(),frame.getTargetId());
        station.mSender.onPostRecvPTS(frame);
    }
}
