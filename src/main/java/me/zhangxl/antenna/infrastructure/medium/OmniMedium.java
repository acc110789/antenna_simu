package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.infrastructure.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangxiaolong on 16/4/10.
 */
class OmniMedium extends Medium {

    static {
        sMedium = new OmniMedium();
    }

    @Override
    List<Role> getStationToReceive(Role station) {
        List<Role> result = new ArrayList<>(stationList);
        result.remove(station);
        return result;
    }
}
