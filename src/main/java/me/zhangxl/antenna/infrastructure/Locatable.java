package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.station.Stateful;
import me.zhangxl.antenna.util.Pair;

/**
 * 可以定位的节点
 * Created by zhangxiaolong on 16/5/12.
 */
public interface Locatable extends Stateful {
    Pair<Double,Double> getAxis();
    int getId();
    boolean beginReceiveFrame(final Frame frame);
}
