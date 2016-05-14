package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.PairFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.station.BaseRole.Status;

/**
 * 接受到正确的PairFrame之后的route
 * SIFS --> DATA --> SIFS --> ACK
 * 刚刚发送完Ack或者刚刚接收完Ack之后立刻切换频率,
 * 切回到只能接收Pcp节点的信号的频率.
 * Created by zhangxiaolong on 16/5/13.
 */
class OnReceivePairFrame extends OnReceiveFrameLogic {

    OnReceivePairFrame(Station station) {
        super(station);
    }

    @Override
    void doLogic(final Frame frame) {
        assert frame instanceof PairFrame;
        assert station.getCurrentStatus() == Status.WAITING_NEXT_ROUND;
        station.setCurrentStatus(Status.RECEIVING_PAIR_FRAME);

        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                assert station.getCurrentStatus() == Status.RECEIVING_PAIR_FRAME;
                station.receivingFrames.remove(frame);
                //接收成功
                if(frame.getSrcId() == station.getId() || frame.getTargetId() == station.getId()){
                    //交给上层处理
                    station.onPaired(frame.getSrcId(),frame.getTargetId(),((PairFrame) frame).getChannel());
                } else {
                    /**
                     * 如果目标节点跟自己没有关系,就还是回到 {@link Status.WAITING_NEXT_ROUND}
                     */
                    station.setCurrentStatus(Status.WAITING_NEXT_ROUND);
                }
            }
        }, frame.getEndDuration());
    }
}
