package me.zhangxl.antenna.infrastructure.base;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.util.Pair;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
public interface Locatable extends Stateful {
    Pair<Double,Double> getAxis();
    int getId();
    boolean beginReceiveFrame(final Frame frame);
}
