package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.base.ChannelManager;
import me.zhangxl.antenna.infrastructure.base.Locatable;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.pcp.PcpStation;
import me.zhangxl.antenna.util.SimuLoggerManager;
import me.zhangxl.antenna.util.TimeLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * 代表传输的介质(信道)
 * Created by zhangxiaolong on 16/3/24.
 */
public class Medium {

    static final TimeLogger logger = SimuLoggerManager.getLogger(Medium.class.getSimpleName());
    public static final int DIRECT_MODE = 1;
    public static final int OMNI_MODE = 2;

    static final List<Locatable> stationList = new ArrayList<>();
    private static DirectMedium directMedium;
    private static OmniMedium omniMedium;
    private static Medium sInstance;

    Medium() {
        TimeController.getInstance().setLoopCallBack(new Runnable() {
            @Override
            public void run() {
                //触发所有的节点
                logger.unLogHeader();
                logger.info("定向天线模式,分析所有Station的位置信息......");
                directMedium.analysisStationLocation();
                logger.logHeader();
                //作为程序开始的起点,让Pcp节点率先发出一个BofFrame
                PcpStation.getInstance().sendBofFrame(-1, false);
            }
        });
    }

    public void register(Locatable station) {
        stationList.add(station);
    }

    public static Medium getInstance() {
        if (directMedium == null) {
            directMedium = new DirectMedium();
        }
        if (omniMedium == null) {
            omniMedium = new OmniMedium();
        }
        if (sInstance == null) {
            sInstance = new Medium();
        }
        return sInstance;
    }

    /**
     * @return 返回节点数量
     */
    public int getPeerNum(){
        return stationList.size() -1;
    }

    public static void reset() {
        directMedium = null;
        stationList.clear();
    }

    /**
     * @param frame 对于一般的frame,判断哪些节点需要接受到这个frame
     */
    public void putFrame(final Locatable source, final Frame frame) {
        frame.setStartTimeNow();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                List<Locatable> targets;
                if (ChannelManager.getInstance().isOmniChannel(frame.getFre())) {
                    //全向频率
                    targets = omniMedium.getStationToReceive(source, frame);
                } else if (ChannelManager.getInstance().isDirectChannel(frame.getFre())) {
                    //定向频率
                    targets = directMedium.getStationToReceive(source, frame);
                } else {
                    throw new IllegalStateException("不可能的频率");
                }

                for (Locatable station1 : targets) {
                    Frame copy;
                    try {
                        copy = (Frame) frame.clone();
                    } catch (CloneNotSupportedException e) {
                        throw new IllegalStateException(e);
                    }
                    station1.beginReceiveFrame(copy);
                }
            }
        }, 0, TimeTask.RECEIVE);
    }

    List<Locatable> getStationToReceive(Locatable station, Frame frame) {
        throw new IllegalStateException();
    }
}
