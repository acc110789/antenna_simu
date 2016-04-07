package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.AckFrame;
import me.zhangxl.antenna.frame.GarbageFrame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.StationUtil;
import me.zhangxl.antenna.infrastructure.clock.ClockController;
import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.RtsFrame;
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
    private static final Medium sMedium = new Medium();

    private AtomicBoolean free = new AtomicBoolean(false);

    private List<RtsFrame> rtsToSend = new ArrayList<>();

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

    public static Medium getInstance() {
        return sMedium;
    }

    /**
     * @param frame 对于一般的frame,可以直接post出去
     */
    public void putFrame(final Frame frame) {
        ClockController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                for (Station station : StationUtil.stationList) {
                    station.receiveFrame(frame);
                }
                if(frame instanceof GarbageFrame || frame instanceof AckFrame) {
                    ClockController.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            Medium.getInstance().setFree();
                        }
                    }, Config.getInstance().getDifs());
                }
            }
        }, frame.getTransmitDuration());//计算要将frame全部全部传输到目标节点所需要的时间
    }

    /**
     * @param frame 对于RtsFrame来说 不能直接ClockController.
     *              getInstance().post(),因为可能有Rts冲突
     */
    public void putRts(RtsFrame frame){
        rtsToSend.add(frame);
        setBusyIfNeed();
    }

    /**
     * 检查碰撞,如果没有发生碰撞,则真正开始发送frame
     */
    void checkCollisionAndSend() {
        if(rtsToSend.size() > 0){
            ClockController.getInstance().addSendTimes();
        }

        if (rtsToSend.size() > 1) {
            ClockController.getInstance().addCollitionTimes();
            if(Logger.DEBUG_COLLISION) {
                logger.log("collision occur");
            }
            //我们认为Station刚开始发送Rts就能立刻知道是否发生了碰撞,而不用等到Rts发送完成才知道
            MediumObservers.getInstance().onRtsCollision(new ArrayList<>(rtsToSend));
            //发送一个垃圾桢
            putFrame(Frame.generateGarbageFrame(new ArrayList<Frame>(rtsToSend)));
            rtsToSend.clear();

        } else if (rtsToSend.size() == 1) {
            //只有一个待发送的frame
            putFrame(rtsToSend.remove(0));
        }
    }


    /**
     * 这个方法标志着Medium刚好空闲了DIFS
     */
    private void setFree() {
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
            },Config.getInstance().getSlotLength());
        }
    }

    private void setBusyIfNeed(){
        if(free.get()) {
            free.set(false);
        }
    }
}
