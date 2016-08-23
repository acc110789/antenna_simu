package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.infrastructure.Locatable;
import me.zhangxl.antenna.infrastructure.host_peer.PcpStation;
import me.zhangxl.antenna.infrastructure.station.Station;

import java.util.ArrayList;
import java.util.List;

import static me.zhangxl.antenna.infrastructure.medium.DirectMedium.sMap;

/**
 * Created by zhangxiaolong on 16/8/23.
 */
public class TargetFilters implements Filter<Locatable> {
    private final Locatable source;
    private final List<Locatable> targets;

    public TargetFilters(Locatable source,List<Locatable> targets){
        this.source = source;
        this.targets = targets;
    }
    @Override
    public List<Locatable> filt() {
        List<Locatable> results = new ArrayList<>();
        for(Locatable target: targets){
            if(target instanceof Station){
                //如果source在target当前指向的扇区内部,则把target留下
                if(sMap.get(target).getStations(((Station) target).getFoucusSector()).contains(source)) {
                    results.add(target);
                }
            } else if(target instanceof PcpStation){
                //PcpStation 是全向接收信息
                results.add(target);
            }
        }
        return results;
    }
}
