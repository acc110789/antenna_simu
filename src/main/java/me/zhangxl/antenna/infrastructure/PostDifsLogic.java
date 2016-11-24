package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.FrameHelper;
import me.zhangxl.antenna.infrastructure.base.Stateful;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.frame_process.SendRtsProcessor;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.SimuLoggerManager;
import me.zhangxl.antenna.util.TimeLogger;
import org.apache.logging.log4j.Logger;

/**
 * 当时间到达DIFS结束的时候,节点要执行的逻辑(也就是恢复backoff)
 * Created by zhangxiaolong on 16/6/14.
 */
public class PostDifsLogic {
    private Logger logger = SimuLoggerManager.getLogger("difs");
    private final Station station;

    public PostDifsLogic(Station station){
        this.station = station;
    }

    public void process() {
        logger.debug("%d onPostDIFS", station.getId());
        assert station.getCurrentStatus() == Stateful.Status.SLOTING;
        if (station.getDataFrameToSend() == null) {
            station.setNextDataFrameToSend();
        } else {
            logger.info("%d current window: %d",station.getId(),station.getDataFrameToSend().getBackOff());
        }
        if(!sendDataIfNeed()){
            scheduleSLOT();
        }
    }

    private void onPostSLOT() {
        logger.debug("%d onPostSLOT", station.getId());
        assert station.getCurrentStatus() == Stateful.Status.SLOTING;
        assert station.getDataFrameToSend() != null;
        station.getDataFrameToSend().countDownBackOff();
        logger.info("%d current window: %d,     target:%d",station.getId(),
                station.getDataFrameToSend().getBackOff(),station.getDataFrameToSend().getTargetId());
        if(!sendDataIfNeed()){
            scheduleSLOT();
        }
    }

    private void scheduleSLOT() {
        assert station.getCurrentStatus() == Stateful.Status.SLOTING;
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if(station.getCurrentStatus() == Stateful.Status.SLOTING) {
                    //有可能Station已经作为接收端开始在接受信息了
                    //这种情况下,不能再执行onPostSLOT()了
                    onPostSLOT();
                }
            }
        }, Config.getSlotLength());
    }

    /**
     * @return 如果确实已经开始发送rts则返回true,如没有开始发送过程返回false
     */
    private boolean sendDataIfNeed() {
        if (station.getDataFrameToSend() != null && station.getDataFrameToSend().canBeSent()) {
            //开始进入流程
            if (TimeLogger.DEBUG_STATION) {
                logger.debug("%d start transmit data frame sendDataIfNeed", station.getId());
            }
            station.getDataFrameToSend().setStartTimeNow();

            new SendRtsProcessor(station).process(FrameHelper.generateRtsFrame(station.getDataFrameToSend()));
            return true;
        }
        return false;
    }
}
