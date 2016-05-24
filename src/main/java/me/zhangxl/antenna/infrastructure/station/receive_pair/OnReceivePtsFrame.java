package me.zhangxl.antenna.infrastructure.station.receive_pair;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;
import me.zhangxl.antenna.infrastructure.station.OnReceiveFrameLogic;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.station.cool.DifsCooler;

/**
 * 接受到正确的PairFrame之后的route
 * SIFS --> DATA --> SIFS --> ACK
 * 刚刚发送完Ack或者刚刚接收完Ack之后立刻切换频率,
 * 切回到只能接收Pcp节点的信号的频率.
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceivePtsFrame extends OnReceiveFrameLogic {

    public OnReceivePtsFrame(Station station) {
        super(station);
    }

    @Override
    public void doLogic(final Frame frame) {
        assert frame instanceof PtsFrame;
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                station.receivingFrames.remove(frame);
                if(!frame.isDirty()){
                    //对于正在进行数据通信的节点以及正在nav中的节点,是不可能收到PtsFrame的
                    Status status = station.getCurrentStatus();
                    if(status == Status.SLOTING || status == Status.WAITING_PTS){
                        //是干净的,且是当前状态期待的桢类型
                        deal((PtsFrame) frame);
                    }
                }
            }
        }, frame.getEndDuration());
    }

    /**
     * 接收到PtsFrame之后根据不同的情况进行相应的处理
     */
    private void deal(PtsFrame frame){
        int srcId = frame.getSrcId();
        int targetId = frame.getTargetId();
        if(station.getId() == srcId){
            new SrcMatchAction(station,frame).action();
        } else if(station.getId() == targetId){
            new TargetMatchAction(station,frame).action();
        } else {
            /**
             * 如果没有关系,就设置NAV
             */
            station.setCurrentStatus(Status.NAVING);
            TimeController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    new DifsCooler(station).cool();
                }
            },frame.getNavDuration(), TimeTask.SEND);
        }
    }
}
