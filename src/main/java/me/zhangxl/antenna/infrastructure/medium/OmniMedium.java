package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Locatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangxiaolong on 16/4/10.
 */
class OmniMedium extends Medium {

    @Override
    List<Locatable> getStationToReceive(Locatable station, Frame frame) {
        List<Locatable> result = new ArrayList<>(stationList);
        result.remove(station);
        return result;
    }
}
