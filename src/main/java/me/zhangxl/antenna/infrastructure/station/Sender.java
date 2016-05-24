package me.zhangxl.antenna.infrastructure.station;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.DataFrame;
import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.cool.SenderDifsCooler;
import me.zhangxl.antenna.infrastructure.station.wait.AckTimeOutWaiter;
import me.zhangxl.antenna.infrastructure.station.wait.SenderPtsTimeOutWaiter;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

/**
 * 一次通信过程中的发送者角色
 * Created by zhangxiaolong on 16/4/15.
 */
public class Sender extends BaseRoleFilter implements SenderExpandRole {

    private static final Logger logger = SimuLoggerManager.getLogger(Sender.class.getSimpleName());
    private final Station mRole;
    private final double ptsTimeOut = PrecisionUtil.add(
            Config.getInstance().getSifs(),
            PtsFrame.getFrameTimeLength(),
            Config.getInstance().getDifs());

    Sender(Station role) {
        super(role);
        this.mRole = role;
    }

    /**
     * @param frame 待发送的RTS
     *              在这个时刻状态从SLOTING变成SENDING_RTS
     */
    @Override
    public void onPreSendRTS(RtsFrame frame) {
        onSendMethod(logger, String.format("%d onPreSendRTS()", getId()),
                Status.SLOTING, Status.SENDING_RTS, new Runnable() {
                    @Override
                    public void run() {
                        onPostSendRTS();
                    }
                }, frame.getTransmitDuration(), TimeTask.SEND);
        setCommunicationTarget(frame.getTargetId());
        sendFrame(frame);
    }

    /**
     * RTS发送完成之后,还需要为PTS超时做准备
     * 设置PTS超时之后执行的代码。
     */
    @Override
    public void onPostSendRTS() {
        onSendMethod(logger, String.format("%d onPostSendRTS()", getId()), Status.SENDING_RTS, null, null, 0.0, 0);
        new SenderPtsTimeOutWaiter(mRole).await();
    }


    private void onPreSendSIFSAndDATA() {
        onSendMethod(logger, String.format("%d onPreSendSIFSAndDATA()", getId()), Status.SENDING_DATA,
                Status.SENDING_DATA, new Runnable() {
                    @Override
                    public void run() {
                        onPreSendData(getDataToSend());
                    }
                }, Config.getInstance().getSifs(), TimeTask.SEND);
    }

    @Override
    public void onPreSendData(DataFrame dataFrame) {
        onSendMethod(logger, String.format("%d onPreSendData()", getId()),
                Status.SENDING_DATA, Status.SENDING_DATA, new Runnable() {
                    @Override
                    public void run() {
                        onPostSendDATA();
                    }
                }, dataFrame.getTransmitDuration(), TimeTask.SEND);
        sendFrame(dataFrame);
    }

    @Override
    public void onPostSendDATA() {
        onSendMethod(logger, String.format("%d onPostSendDATA()", getId()), Status.SENDING_DATA,
                Status.WAITING_ACK, null, 0.0, 0);
        new AckTimeOutWaiter(mRole).await();
    }

    /**
     * 在真实的环境下,只有没有被碰撞的frame才能收到,所以当发生碰撞时,不打log
     * {@link #onPreSendSIFSAndDATA()}
     * @param frame
     */
    @Override
    public void onPostRecvPTS(PtsFrame frame) {
        onPostRecvMethod(logger, String.format("%d onPostRecvPTS()", getId()),
                frame, Status.WAITING_PTS, Status.SENDING_DATA, new Runnable() {
                    @Override
                    public void run() {
                        onPreSendSIFSAndDATA();
                    }
                });
        throw new IllegalStateException();
    }

    @Override
    public void onPostRecvACK(AckFrame frame) {
        onPostRecvMethod(logger, String.format("%d onPostRecvACK()", getId()),
                frame, Status.WAITING_ACK, null, null);
        new SenderDifsCooler(mRole).cool();
    }

    @Override
    public void onSendSuccess() {
        mRole.onSendSuccess();
    }
}
