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
                for(Locatable locatable : stationList){
                    if(locatable instanceof Station){
                        ((Station) locatable).setDefaultSector();
                    }
                }
                for(Locatable locatable : stationList){
                    if(locatable instanceof Station){
                        ((Station) locatable).onPostDIFS();
                    }
                }
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

    public void putFrame(final Locatable source, final Frame frame, final int sector){
        frame.setStartTimeNow();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                //定向频率
                List<Locatable> targets = directMedium.getStationToReceive(source, frame , sector);
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
