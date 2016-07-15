package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by zhangxiaolong on 16/5/13.
 */
public class SlotManager {
    private static final Logger logger = SimuLoggerManager.getLogger(Station.class.getSimpleName());
    private final Station station;
    private int mSlotCount;

    public SlotManager(Station station) {
        this.station = station;
    }

    public void setAvailableSlotCount(int count) {
        this.mSlotCount = count;
    }

    /**
     * 安排下一个slot的时间点
     */
    public void scheduleSLOT() {
        assert station.getCurrentStatus() == Status.SLOTING;
        if (mSlotCount > 0) {
            mSlotCount--;
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    assert station.getCurrentStatus() == Status.SLOTING;
                    onPostSLOT();
                }
            }, Config.getInstance().getSlotLength());
        } else {
            station.setCurrentStatus(Status.WAITING_NEXT_ROUND);
        }
    }

    /**
     * 到了slot的时间点了
     */
    private void onPostSLOT() {
        logger.debug("%d onPostSLOT", station.getId());
        assert station.getDataFrameToSend() != null;
        station.getDataFrameToSend().countDownBackOff();
        logger.info("%d current window: %d,     target:%d",station.getId(),
                station.getDataFrameToSend().getBackOff(),station.getDataFrameToSend().getTargetId());
        if (!station.sendDataIfNeed()) {
            scheduleSLOT();
        }
    }
}
