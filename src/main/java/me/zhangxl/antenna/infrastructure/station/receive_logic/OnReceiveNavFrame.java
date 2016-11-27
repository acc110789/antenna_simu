package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.NavFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.nav.FrameNav;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by zhangxiaolong on 16/7/14.
 */
public class OnReceiveNavFrame extends OnReceiveFrameLogic {
    private static Logger logger = SimuLoggerManager.getLogger("receiveNav");

    public OnReceiveNavFrame(Station station, Frame frame) {
        super(station, frame);
    }

    @Override
    void onPre() {
        assert frame instanceof NavFrame;
        assert station.getCurrentStatus() == Status.WAITING_BACK_OFF;
        station.setCurrentStatus(Status.RECEIVING_NAV_FRAME);
    }

    @Override
    void onPost() {
        //如果这个frame的确是发送给自己的那么就执行NavFrame指定的nav时间
        //或者这个frame是发送给所有人(即targetId小于0)
        assert station.getCurrentStatus() == Status.RECEIVING_NAV_FRAME;
        if(frame.getTargetId() == station.getId() || frame.getTargetId()<0) {
            logger.info("%d receive nav frame",station.getId());
            new FrameNav(station, (NavFrame) frame).startNav();
        }
    }
}