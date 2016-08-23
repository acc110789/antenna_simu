package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.infrastructure.Locatable;
import me.zhangxl.antenna.infrastructure.host_peer.PcpStation;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

import java.util.List;

import static me.zhangxl.antenna.infrastructure.medium.DirectMedium.getAngle;
import static me.zhangxl.antenna.infrastructure.medium.DirectMedium.sMap;
import static me.zhangxl.antenna.infrastructure.medium.Medium.stationList;

/**
 * Created by zhangxiaolong on 16/8/23.
 */
class SourceFilterByFrame implements Filter<Locatable> {
    private final Locatable source;
    private final Frame frame;

    SourceFilterByFrame(Locatable source, Frame frame){
        this.frame = frame;
        this.source = source;
    }

    @Override
    public List<Locatable> filt() {
        Locatable target = null;
        if(frame instanceof RtsFrame){
            target = PcpStation.getInstance();
        } else {
            int targetId = frame.getTargetId();
            for (Locatable station : stationList) {
                if (station.getId() == targetId) {
                    target = station;
                }
            }
        }
        //找到target所在的扇区的index
        assert target != null;
        double angle = getAngle(target.getAxis(), source.getAxis());
        double unit = PrecisionUtil.div(360, Config.getInstance().getPart());
        double d_index = PrecisionUtil.div(angle, unit);
        int index = ((int) Math.floor(d_index));
        //返回该index扇区内的所有节点
        return sMap.get(source).getStations(index);
    }
}
