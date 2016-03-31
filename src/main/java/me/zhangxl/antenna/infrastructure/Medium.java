package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.application.App;
import me.zhangxl.antenna.request.Frame;
import me.zhangxl.antenna.request.RtsFrame;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 代表传输的介质(信道)
 * Created by zhangxiaolong on 16/3/24.
 */
public class Medium {

    private static final Logger logger = new Logger(Medium.class);

    public static final List<App> appList = new ArrayList<>();

    private static final Medium sMedium = new Medium();

    private AtomicBoolean busy = new AtomicBoolean(false);

    private List<Frame> frameToSend = new ArrayList<>();

    static Medium getInstance() {
        return sMedium;
    }

    private Medium() {}

    /**
     * @param frame 不能直接ClockController.getInstance().post(),因为可能有Rts冲突
     */
    void putFrame(final Frame frame) {
        frameToSend.add(frame);
        setBusyIfNeed();
    }

    /**
     * 真正发送frame的地方
     */
    void onPostTask() {
        if (frameToSend.size() > 1) {
            logger.log("conflict occur");
            for (Frame frame : frameToSend) {
                if(!(frame instanceof RtsFrame)){
                    //发生冲突的frame只能是RtsFrame,如果不是,则抛出错误
                    throw new IllegalStateException("conflict frame type is not RtsFrame,but "
                            +frame.getClass().getSimpleName());
                }
            }
            List<RtsFrame> frames = new ArrayList<>();
            for(Frame frame : frameToSend){
                frames.add((RtsFrame) frame);
            }
            MediumObservers.getInstance().onRtsCollision(frames);
            ClockController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    setFree();
                }
            },frameToSend.get(0).getTransmitDuration() + Config.DIFS);
            frameToSend.clear();

        } else if (frameToSend.size() == 1) {
            //只有一个待发送的frame
            final Frame frame = frameToSend.remove(0);
            ClockController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    for (App app : appList) {
                        app.getStation().receiveFrame(frame);
                    }
                }
            }, frame.getTransmitDuration());//计算要将frame全部全部传输到目标节点所需要的时间
        }
    }


    /**
     * 这个方法标志着Medium刚好空闲了DIFS
     */
    void setFree() {
        busy.set(false);
        MediumObservers.getInstance().onPostDifs();//有可能信道在postDifs的过程中又被占用了
        notifyNewSlot();
    }

    private void notifyNewSlot(){
        if(!busy.get()){
            //如果信道并没有被占有,则每当有slot的时候需要进行通知
            ClockController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    MediumObservers.getInstance().onNewSLot();
                    notifyNewSlot();
                }
            },Config.SLOT_LENGTH);
        }
    }

    private void setBusyIfNeed(){
        if(!busy.get()) {
            busy.set(true);
        }
    }
}
