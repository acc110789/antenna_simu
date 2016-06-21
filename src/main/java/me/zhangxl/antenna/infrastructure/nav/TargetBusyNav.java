package me.zhangxl.antenna.infrastructure.nav;

import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.util.Constant;

/**
 * 这个nav是说收到一个PairFrame,并且这个PairFrame
 * 里面没有没有允许自己的发送请求,相反,自己的发送目标
 * 在请求之列。因此应该设置nav。
 * Created by zhangxiaolong on 16/5/26.
 */
public class TargetBusyNav extends AbstractNav {
    public TargetBusyNav(Station station) {
        super(station);
    }

    @Override
    double getNavDuration() {
        // TODO: 16/6/21 查一查这个类的功能和DataNav是否是一致的
        return Constant.getDataNav();
    }
}
