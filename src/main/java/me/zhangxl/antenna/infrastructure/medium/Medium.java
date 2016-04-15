package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Role;
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

    static final List<Role> stationList = new ArrayList<>();

    /**
     * 把station没有接受(由于station)的frame暂时放置在这里
     */
    static final Map<Role,List<Frame>> stationToFrames = new HashMap<>();

    private static final Logger logger = new Logger(Medium.class);

    static  Medium sMedium ;

    private AtomicBoolean free = new AtomicBoolean(false);

    Medium() {
        TimeController.getInstance().setLoopCallBack(new Runnable() {
            @Override
            public void run() {
                //触发所有的节点
                for(Role station : stationList){
                    station.scheduleDIFS(true);
                }
            }
        });
    }


    public void register(Station station){
        stationList.add(station);
    }

    public static Medium getInstance() {
        try {
            Class.forName("me.zhangxl.antenna.infrastructure.medium.OmniMedium");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        return sMedium;
    }

    /**
     * @param station
     * @param frame 对于一般的frame,判断哪些节点需要接受到这个frame
     */
    public void putFrame(final Role station, final Frame frame) {
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                for(Role station1 : getStationToReceive(station)){
                    boolean accepted = ((Station)station1).beginReceiveFrame(frame);
                    if(!accepted){
                        putUnacceptedFrames(station1,frame);
                    }
                }
            }
        },0);
    }

    private void putUnacceptedFrames(final Role station , final Frame frame){
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
                double endTime = frame.getStartTime() + frame.getTransmitDuration();
                double currentTime = TimeController.getInstance().getCurrentTime();
                if(endTime < currentTime){
                    //这个frame本来应该是已经消失的
                    throw new IllegalStateException("impossible state");
                } else if(frame.getStartTime() <= currentTime && currentTime < endTime){
                    //是否是一个残废的frame应该由Station自己去判断
                    station.beginReceiveFrame(frame);
                } else if(currentTime < frame.getStartTime()){
                    throw new IllegalStateException();
                }
                //当endTime == currentTime时 可以认为数据已经传输完毕,此处不再进行数据的传输了

            }
        }
    }

    abstract List<Role> getStationToReceive(Role station);


}
