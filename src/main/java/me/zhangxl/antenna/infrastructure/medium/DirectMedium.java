package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.infrastructure.Role;

import java.util.List;

/**
 * Created by zhangxiaolong on 16/4/10.
 */
public class DirectMedium extends Medium {
    @Override
    List<Role> getStationToReceive(Role station) {
        return null;
    }
}
