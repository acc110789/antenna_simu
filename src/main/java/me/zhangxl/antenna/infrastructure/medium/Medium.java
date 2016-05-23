package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Locatable;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.util.SimuLoggerManager;
import me.zhangxl.antenna.util.TimeLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代表传输的介质(信道)
 * Created by zhangxiaolong on 16/3/24.
 */
public class Medium {

    static final TimeLogger logger = SimuLoggerManager.getLogger(Medium.class.getSimpleName());
    public static final int DIRECT_MODE = 1;
    public static final int OMNI_MODE = 2;

    static final List<Locatable> stationList = new ArrayList<>();
    /**
     * 把station没有接受(由于station)的frame暂时放置在这里
     */
    static final Map<Locatable, List<Frame>> stationToFrames = new HashMap<>();
    private static DirectMedium directMedium;
    private static OmniMedium omniMedium;
    private static Medium sInstance;

    Medium() {
        logger.unLogHeader();
        TimeController.getInstance().setLoopCallBack(new Runnable() {
            @Override
            public void run() {
                //触发所有的节点
                logger.info("定向天线模式,分析所有Station的位置信息......");
                directMedium.analysisStationLocation();
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
        stationToFrames.clear();
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
                //定向频率
                List<Locatable> targets = directMedium.getStationToReceive(source, frame);
                for (Locatable station1 : targets) {
                    Frame copy;
                    try {
                        copy = (Frame) frame.clone();
                    } catch (CloneNotSupportedException e) {
                        throw new IllegalStateException(e);
                    }
                    boolean accepted = station1.beginReceiveFrame(copy);
                    if (!accepted) {
                        putUnacceptedFrames(station1, copy);
                    }
                }
            }
        }, 0, TimeTask.RECEIVE);
    }

    private void putUnacceptedFrames(final Locatable station, final Frame frame) {
        List<Frame> frames = stationToFrames.get(station);
        if (frames == null) {
            frames = new ArrayList<>();
            stationToFrames.put(station, frames);
        }
        frames.add(frame);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                List<Frame> frames1 = stationToFrames.get(station);
                frames1.remove(frame);
            }
        }, frame.getEndDuration());
    }

    public void notify(Station station) {
        List<Frame> frames = stationToFrames.get(station);
        if (frames != null && frames.size() > 0) {
            for (Frame frame : frames) {
                double endTime = frame.getEndTime();
                double currentTime = TimeController.getInstance().getCurrentTime();
                if (endTime < currentTime) {
                    //这个frame本来应该是已经消失的
                    System.out.println();
                    System.out.println((endTime - currentTime) < 0);
                    String info = String.format("endTime:%#.16f  currentTime:%#.16f", endTime, currentTime);
                    throw new IllegalStateException(info);
                } else if (frame.getStartTime() <= currentTime && currentTime < endTime) {
                    //是否是一个残废的frame应该由Station自己去判断
                    station.beginReceiveFrame(frame);
                } else if (currentTime < frame.getStartTime()) {
                    throw new IllegalStateException();
                }
                //当endTime == currentTime时 可以认为数据已经传输完毕,此处不再进行数据的传输了

            }
        }
    }

    List<Locatable> getStationToReceive(Locatable station, Frame frame) {
        throw new IllegalStateException();
    }
}
