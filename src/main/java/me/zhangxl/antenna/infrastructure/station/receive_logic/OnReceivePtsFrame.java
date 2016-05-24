package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.station.cool.DifsCooler;
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

    /**
     * 接收到PtsFrame之后根据不同的情况进行相应的处理
     */
    @Override
    public void onClearFrame() {
        int srcId = frame.getSrcId();
        int targetId = frame.getTargetId();
        if(station.getId() == srcId){
            new SrcMatchAction(station, (PtsFrame) frame).action();
        } else if(station.getId() == targetId){
            new TargetMatchAction(station, (PtsFrame) frame).action();
        } else if(((PtsFrame)frame).isPassByPcp() || isTargetTakenOver((PtsFrame) frame)) {
            /**
             * 如果没有关系,就设置NAV
             */
            new PtsNav(station, (PtsFrame) frame).startNav();
        } else {
            //其它情况的话就当没有收到这个PtsFrame,需要进行状态变更
            new DifsCooler(station).cool();
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
