package me.zhangxl.antenna.infrastructure.station.receive_logic.onpair;

import me.zhangxl.antenna.infrastructure.nav.TargetBusyNav;
import me.zhangxl.antenna.infrastructure.station.Station;

/**
 * PairFrame的srcId或者targetId和本station即将要发送的
 * DataFrame的targetId相同
 * Created by zhangxiaolong on 16/5/15.
 */
public class TargetRelatedPairAction extends AbstractPairAction {

    public TargetRelatedPairAction(Station station) {
        super(station);
    }

    @Override
    public void action() {
        new TargetBusyNav(station).nav();
    }
}
