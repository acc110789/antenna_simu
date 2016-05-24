package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.station.nav.PtsNav;
import me.zhangxl.antenna.infrastructure.station.receive_pair.SrcMatchAction;
import me.zhangxl.antenna.infrastructure.station.receive_pair.TargetMatchAction;

/**
 * 接受到正确的PairFrame之后的route
 * SIFS --> DATA --> SIFS --> ACK
 * 刚刚发送完Ack或者刚刚接收完Ack之后立刻切换频率,
 * 切回到只能接收Pcp节点的信号的频率.
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceivePtsFrame extends OnReceiveFrameLogic {

    public OnReceivePtsFrame(Station station, Frame frame) {
        super(station, frame);
    }

    @Override
    void onPreFrame() {
        assert frame instanceof PtsFrame;
    }

    @Override
    public void onClearFrame() {
        //对于正在进行数据通信的节点以及正在nav中的节点,是不可能收到PtsFrame的
        Status status = station.getCurrentStatus();
        if(status == Status.SLOTING || status == Status.WAITING_PTS){
            //是干净的,且是当前状态期待的桢类型
            deal((PtsFrame) frame);
        }
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
        } else if(frame.isPassByPcp() || isTargetTakenOver(frame)) {
            /**
             * 如果没有关系,就设置NAV
             */
            new PtsNav(station,frame).startNav();
        }
    }

    /**
     * @param frame ptsframe
     * @return 如果本station要传输的dataFrame的目标地址恰好是ptsframe
     * 中指定的src或者target之一,则返回true
     */
    private boolean isTargetTakenOver(PtsFrame frame){
        int targetId  = station.getDataToSend().getTargetId();
        return targetId == frame.getSrcId() || targetId == frame.getTargetId();
    }
}