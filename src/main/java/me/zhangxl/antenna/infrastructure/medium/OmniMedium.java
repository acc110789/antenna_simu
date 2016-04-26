package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangxiaolong on 16/4/10.
 */
class OmniMedium extends Medium {

    @Override
    List<Station> getStationToReceive(Station station, Frame frame) {
        List<Station> result = new ArrayList<>(stationList);
        result.remove(station);
        return result;
    }
}
