package me.zhangxl.antenna.infrastructure.station.receive_pair;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.PairFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;
import me.zhangxl.antenna.infrastructure.station.OnReceiveFrameLogic;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * 接受到正确的PairFrame之后的route
 * SIFS --> DATA --> SIFS --> ACK
 * 刚刚发送完Ack或者刚刚接收完Ack之后立刻切换频率,
 * 切回到只能接收Pcp节点的信号的频率.
 * Created by zhangxiaolong on 16/5/13.
 */
public class OnReceivePairFrame extends OnReceiveFrameLogic {

    public OnReceivePairFrame(Station station) {
        super(station);
    }

    @Override
    public void doLogic(final Frame frame) {
        assert frame instanceof PairFrame;
        assert station.getCurrentStatus() == Status.WAITING_NEXT_ROUND;
        station.setCurrentStatus(Status.RECEIVING_PAIR_FRAME);

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                assert station.getCurrentStatus() == Status.RECEIVING_PAIR_FRAME;
                station.receivingFrames.remove(frame);
                //接收成功
                deal((PairFrame) frame);
                if(frame.getSrcId() == station.getId() || frame.getTargetId() == station.getId()){
                    //交给上层处理
                    station.onPaired(frame.getSrcId(),frame.getTargetId(),((PairFrame) frame).getChannel());
                }
            }
        }, frame.getEndDuration());
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
            int sendTarget = station.mCurrentSendingFrame.getTargetId();
            if(sendTarget == srcId || sendTarget == targetId){
                new TargetRelatedPairAction(station).action();
            }
        }
    }
}
