package me.zhangxl.antenna.infrastructure.nav;

import me.zhangxl.antenna.frame.SnavFrame;
import me.zhangxl.antenna.infrastructure.Station;

/**
 * Created by zhangxiaolong on 16/7/14.
 */
public class FrameNav extends AbstractNav {
    private final SnavFrame frame;
    public FrameNav(Station station,SnavFrame frame) {
        super(station);
        this.frame = frame;
    }

    @Override
    double getNavDuration() {
        return frame.getNavDuration();
    }
}
