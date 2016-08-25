package me.zhangxl.antenna.infrastructure.station.receive_logic.onpair;

import me.zhangxl.antenna.frame.OtcFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.frame_process.OtcProcessor;

/**
 * station 接收到一个OtcFrame,
 * 这个OtcFrame中的srcId是本station。
 * Created by zhangxiaolong on 16/5/15.
 */
public class SrcMatchPairAction extends AbstractPairAction {

    private final OtcFrame frame;
    public SrcMatchPairAction(Station station, OtcFrame frame) {
        super(station);
        this.frame = frame;
    }

    @Override
    public void action() {
        station.setAcceptFre(frame.getChannel());
        station.getDataFrameToSend().setFre(frame.getChannel());
        station.setCommunicationTarget(frame.getTargetId());
        new OtcProcessor(station).process(frame);
    }
}
