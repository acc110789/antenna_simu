package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.util.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhangxiaolong on 16/4/7.
 */
public class StationUtil {

    private static Random random = new Random(System.currentTimeMillis());

    static final List<Station> stationList = new ArrayList<>();

    public static void clear(){
        stationList.clear();
    }

    //保证每个Station有足够的数据待发送,使之饱和
    public static void guaranteeEnoughFrame(Station station) {
        for (int i = station.getWaitingRequestNum(); i < 100; i++) {
            station.putDataFrame(getRandomTargetId(station.getId()), Config.getInstance().getFixDataLength());
        }
    }

    /**
     * @param id 将从候选范围被排除的Station的id
     * @return 在stationList中随机选取一个id不是 @param id 的Station,并返回其id
     */
    private static int getRandomTargetId(int id) {
        while (true) {
            int randomIndex = random.nextInt(stationList.size());
            int targetId = stationList.get(randomIndex).getId();
            if (targetId != id) {
                return targetId;
            }
        }
    }

    /**
     * @return frame1 和 frame2有重叠部分,即使没有交集,表明不会发生碰撞
     */
    static boolean hasIntersection(Frame frame1, Frame frame2){
        return frame2.getStartTime() < frame1.getEndTime()
                && frame1.getStartTime() < frame2.getEndTime();
    }
}
