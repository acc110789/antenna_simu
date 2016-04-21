package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    static  Medium sMedium ;

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

    public void clear(){
        stationToFrames.clear();
        stationList.clear();
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
     * @param frame 对于一般的frame,判断哪些节点需要接受到这个frame
     */
    public void putFrame(final Station station, final Frame frame) {
        frame.setStartTimeNow();
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                for(Station station1 : getStationToReceive(station)){
                    Frame copy;
                    try {
                        copy = (Frame) frame.clone();
                    } catch (CloneNotSupportedException e) {
                        throw new IllegalStateException(e);
                    }
                    boolean accepted = station1.beginReceiveFrame(copy);
                    if(!accepted){
                        putUnacceptedFrames(station1,copy);
                    }
                }
            }
        },0, TimeTask.RECEIVE);
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
        },frame.getEndDuration());
    }

    public void notify(Station station){
        List<Frame> frames = stationToFrames.get(station);
        if(frames != null && frames.size() > 0){
            for(Frame frame : frames){
                double endTime = frame.getEndTime();
                double currentTime = TimeController.getInstance().getCurrentTime();
                if(endTime < currentTime){
                    //这个frame本来应该是已经消失的
                    System.out.println();
                    System.out.println((endTime-currentTime)<0);
                    String info = String.format("endTime:%#.16f  currentTime:%#.16f",endTime,currentTime);
                    throw new IllegalStateException(info);
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

    abstract List<Station> getStationToReceive(Station station);


}
