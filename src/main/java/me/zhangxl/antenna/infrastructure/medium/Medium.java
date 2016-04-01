package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.clock.ClockController;
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

    public static final List<Station> stationList = new ArrayList<>();

    private static final Medium sMedium = new Medium();

    private AtomicBoolean free = new AtomicBoolean(false);

    private List<Frame> frameToSend = new ArrayList<>();

    public static Medium getInstance() {
        return sMedium;
    }

    private Medium() {
        ClockController.getInstance().setLoopCallBack(new Runnable() {
            @Override
            public void run() {
                //初始化为free
                //这里也是任务串的入口点
                setFree();
            }
        });
    }


    /**
     * @param frame 对于一般的frame,可以直接post出去
     */
    public void putFrame(final Frame frame) {
        ClockController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                for (Station station : stationList) {
                    station.receiveFrame(frame);
                }
            }
        }, frame.getTransmitDuration());
    }

    /**
     * @param frame 对于RtsFrame来说 不能直接ClockController.
     *              getInstance().post(),因为可能有Rts冲突
     */
    public void putRts(RtsFrame frame){
        frameToSend.add(frame);
        setBusyIfNeed();
    }

    /**
     * 检查碰撞,如果没有发生碰撞,则真正开始发送frame
     */
    void checkCollisionAndSend() {
        if (frameToSend.size() > 1) {
            if(Logger.DEBUG_COLLISION) {
                logger.log("collision occur");
            }
            for (Frame frame : frameToSend) {
                if(!(frame instanceof RtsFrame)){
                    //发生冲突的frame只能是RtsFrame,如果不是,则抛出错误
                    throw new IllegalStateException("collision frame type is not RtsFrame,but "
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
                    for (Station station : stationList) {
                        station.receiveFrame(frame);
                    }
                }
            }, frame.getTransmitDuration());//计算要将frame全部全部传输到目标节点所需要的时间
        }
    }


    /**
     * 这个方法标志着Medium刚好空闲了DIFS
     */
    public void setFree() {
        free.set(true);
        MediumObservers.getInstance().onPostDifs();//有可能信道在postDifs的过程中又被占用了
        scheduleNewSlot();
    }

    //如果信道并没有被占有,则当下一个slot出现的时候予以通知
    private void scheduleNewSlot(){
        if(free.get()){
            ClockController.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    MediumObservers.getInstance().onNewSLot();
                    scheduleNewSlot();
                }
            },Config.SLOT_LENGTH);
        }
    }

    private void setBusyIfNeed(){
        if(free.get()) {
            free.set(false);
        }
    }
}
