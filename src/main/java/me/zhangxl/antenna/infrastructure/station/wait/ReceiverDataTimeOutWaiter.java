package me.zhangxl.antenna.infrastructure.station.wait;

import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.infrastructure.station.Stateful.Status;
import me.zhangxl.antenna.infrastructure.station.Stateful;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * Created by zhangxiaolong on 16/5/24.
 */
public class ReceiverDataTimeOutWaiter extends AbstractWaiter {

    private static final double timeout = PrecisionUtil.add(
            Config.getInstance().getSifs(),
            DataFrame.getFrameTimeLength(),
            Config.getInstance().getDifs()
    );

    public ReceiverDataTimeOutWaiter(Station station) {
        super(station);
    }

    @Override
    double getTimeOut() {
        return timeout;
    }

    @Override
    Status getWaitingStatus() {
        return Stateful.Status.WAITING_DATA;
    }

    @Override
    String getInfoToLog() {
        return String.format("%d wait data timeout",station.getId());
    }
}
