package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 代表传输的介质(信道)
 * Created by zhangxiaolong on 16/3/24.
 */
public abstract class Medium {

    static final List<Station> stationList = new ArrayList<>();

    /**
     * 把station没有接受(由于station)的frame暂时放置在这里
     */
    static final Map<Station,List<Frame>> stationToFrames = new HashMap<>();

    private static final Logger logger = new Logger(Medium.class);

    static  Medium sMedium ;

    private AtomicBoolean free = new AtomicBoolean(false);

    Medium() {
        TimeController.getInstance().setLoopCallBack(new Runnable() {
            @Override
            public void run() {
                //触发所有的节点
                for(Station station : stationList){
                    station.onPostDIFS();
                }
            }
        });
    }


    public void register(Station station){
        stationList.add(station);
    }

    public static Medium getInstance() {
        return sMedium;
    }

    /**
     * @param frame 对于一般的frame,判断哪些节点需要接受到这个frame
     *              然后发送给这些节点
     */
    public void putFrame(Station station,final Frame frame) {
        for(Station station1 : getStationToReceive(station)){
            boolean accepted = station1.beginReceiveFrame(frame);
            if(!accepted){
                putUnacceptedFrames(station1,frame);
            }
        }
    }

    private void putUnacceptedFrames(final Station station , final Frame frame){
        List<Frame> frames = stationToFrames.get(station);
        if(frames == null){
            frames = new ArrayList<>();
            stationToFrames.put(station,frames);
        }
        frames.add(frame);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                List<Frame> frames1 = stationToFrames.get(station);
                frames1.remove(frame);
            }
        },frame.getTransmitDuration());
    }

    public void notify(Station station){
        List<Frame> frames = stationToFrames.get(station);
        if(frames != null && frames.size() > 0){
            for(Frame frame : frames){
                station.beginReceiveFrame(frame);
            }
        }
    }

    abstract List<Station> getStationToReceive(Station station);


}
