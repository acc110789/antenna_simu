package me.zhangxl.antenna.infrastructure.frame_process;

import me.zhangxl.antenna.infrastructure.cool.DifsCool;
import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.infrastructure.nav.CtsNav;
import me.zhangxl.antenna.infrastructure.nav.DataNav;
import me.zhangxl.antenna.infrastructure.nav.RtsNav;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;


/**
 * 这里指定一个原则:如果在不恰当的阶段收到了一个完全不相关的桢
 * 则设置这个桢对应的nav。
 * 原来的原则是直接ignore。
 * Created by zhangxiaolong on 16/6/1.
 */
abstract class AbstractProcessor implements Processor {
    final static Logger  logger = SimuLoggerManager.getLogger("process");
    final Station station;
    AbstractProcessor(Station station){
        this.station = station;
    }

    void  sendFrame(Frame frame){
        try {
            Medium.getInstance().putFrame(station, (Frame) frame.clone());
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void process(Frame frame) {
        if(station.getCurrentStatus() != getRightStatus()){
            //比如如果接受rtsFrame,则station此刻的状态必须是recievingRts
            //即接收的桢类型必须和station的状态相符合,如果不符,则设置nav
            //并直接返回
            logger.error("error wrong status");
            setNav(frame);
        } else {
            processInner(frame);
        }
    }

    abstract void processInner(Frame frame);

    abstract Status getRightStatus();

    private void setNav(Frame frame){
        if(station.getCurrentStatus().isSender()){
            station.onFail();
        }
        if(frame instanceof RtsFrame){
            new RtsNav(station).startNav();
        } else if(frame instanceof CtsFrame){
            new CtsNav(station).startNav();
        } else if(frame instanceof DataFrame){
            new DataNav(station).startNav();
        } else if(frame instanceof AckFrame){
            new DifsCool(station).cool();
        }
    }

    /**
     * @param frame 目标frame
     * @return 检查frame是不是来自通信目标,是不是通信目标发给自己的
     */
    boolean needNavById(Frame frame){
        if (station.getCommunicationTarget() != frame.getSrcId()) {
            logger.debug("%d this frame is not from its' communication target :%d",
                    station.getId(), station.getCommunicationTarget());
            return true;
        } else if (station.getId() != frame.getTargetId()) {
            logger.debug("%d this frame from %d is not sent to %d", frame.getSrcId(), station.getId());
            return true;
        }
        return false;
    }

}
