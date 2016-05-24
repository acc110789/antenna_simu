package me.zhangxl.antenna.infrastructure.station.nav;

import me.zhangxl.antenna.frame.PtsFrame;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * 一个节点收到PTS之后,发现PTS的src和target
 * 都不是自己,或者PTS要求所有不相关的节点必须设置NAV
 * Created by zhangxiaolong on 16/5/24.
 */
public class PtsNav extends AbstractNav {

    private final PtsFrame frame;
    //由于PTS有可能是有不同的nav值因此需要将具体的PtsFrame
    //当作参数传进来
    public PtsNav(Station station, PtsFrame frame) {
        super(station);
        this.frame = frame;
    }

    @Override
    double getNavDuration() {
        return frame.getNavDuration();
    }
}
