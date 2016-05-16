package me.zhangxl.antenna.infrastructure.station.receive_pair;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.Constant;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

/**
 * PairFrame的srcId或者targetId和本station即将要发送的
 * DataFrame的targetId相同
 * Created by zhangxiaolong on 16/5/15.
 */
public class TargetRelatedPairAction extends AbstractPairAction {
    private static final Logger logger = SimuLoggerManager.getLogger(Station.class.getSimpleName());

    public TargetRelatedPairAction(Station station) {
        super(station);
    }

    @Override
    public void action() {
        station.setCurrentStatus(Status.NAVING);
        logger.info(String.format("%d set nav",station.getId()));

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                logger.info(String.format("%d unset nav",station.getId()));
                station.setCurrentStatus(Status.WAITING_NEXT_ROUND);
            }
        }, Constant.getDataChannelDeadLine(), TimeTask.SEND);
    }
}
