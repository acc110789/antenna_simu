package me.zhangxl.antenna.infrastructure.station.wait;

import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.infrastructure.station.Stateful;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * Created by zhangxiaolong on 16/5/23.
 */
public class SenderPtsTimeOutWaiter extends AbstractWaiter {
    private static final double ptsTimeOut = PrecisionUtil.add(
            Config.getInstance().getSifs(),
            PtsFrame.getFrameTimeLength(),
            Config.getInstance().getDifs());

    public SenderPtsTimeOutWaiter(Station station) {
        super(station);
    }

    @Override
    double getTimeOut() {
        return ptsTimeOut;
    }

    @Override
    String getInfoToLog() {
        return String.format("%d after onPostSendRTS() wait PTS timeout",station.getId());
    }

    @Override
    Stateful.Status getWaitingStatus() {
        return Stateful.Status.WAITING_PTS;
    }
}
