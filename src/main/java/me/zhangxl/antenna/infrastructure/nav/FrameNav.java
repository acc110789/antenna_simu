package me.zhangxl.antenna.infrastructure.nav;

import me.zhangxl.antenna.frame.NavFrame;
import me.zhangxl.antenna.infrastructure.Station;

/**
 * Created by zhangxiaolong on 16/7/14.
 */
public class FrameNav extends AbstractNav {
    private final NavFrame frame;
    public FrameNav(Station station,NavFrame frame) {
        super(station);
        this.frame = frame;
    }

    @Override
    double getNavDuration() {
        return frame.getNavDuration();
    }
}
