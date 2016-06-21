package me.zhangxl.antenna.infrastructure.station.receive_logic;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.PairFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.base.Stateful.Status;
import me.zhangxl.antenna.infrastructure.station.receive_logic.onpair.SrcMatchPairAction;
import me.zhangxl.antenna.infrastructure.station.receive_logic.onpair.TargetMatchPairAction;
import me.zhangxl.antenna.infrastructure.station.receive_logic.onpair.TargetRelatedPairAction;

/**
 * 接受到正确的PairFrame之后的route
 * SIFS --> DATA --> SIFS --> ACK
 * 刚刚发送完Ack或者刚刚接收完Ack之后立刻切换频率,
 * 切回到只能接收Pcp节点的信号的频率.
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceivePairFrame extends OnReceiveFrameLogic {

    public OnReceivePairFrame(Station station, Frame frame) {
        super(station, frame);
    }

    @Override
    void onPre() {
        assert frame instanceof PairFrame;
        assert station.getCurrentStatus() == Status.WAITING_NEXT_ROUND;
        station.setCurrentStatus(Status.RECEIVING_PAIR_FRAME);
    }

    @Override
    void onPost() {
        assert station.getCurrentStatus() == Status.RECEIVING_PAIR_FRAME;
        deal((PairFrame) frame);
    }

    /**
     * 接收到PairFrame之后根据不同的情况进行相应的处理
     */
    private void deal(PairFrame frame){
        int srcId = frame.getSrcId();
        int targetId = frame.getTargetId();
        if(station.getId() == srcId){
            new SrcMatchPairAction(station,frame).action();
        } else if(station.getId() == targetId){
            new TargetMatchPairAction(station,frame).action();
        } else {
            /**
             * 如果没有关系,就还是回到 {@link Status.WAITING_NEXT_ROUND}
             */
            station.setCurrentStatus(Status.WAITING_NEXT_ROUND);
            if(station.getDataFrameToSend() != null){
                int sendTarget = station.getDataFrameToSend().getTargetId();
                if(sendTarget == srcId || sendTarget == targetId){
                    new TargetRelatedPairAction(station).action();
                }
            }
        }
    }
}
