package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.infrastructure.Locatable;
import me.zhangxl.antenna.util.Config;

import java.util.List;

import static me.zhangxl.antenna.infrastructure.medium.DirectMedium.sMap;

/**
 * Created by zhangxiaolong on 16/8/23.
 */
public class SourceFilterBySector implements Filter<Locatable> {
    private final Locatable source;
    private final int sector;

    public SourceFilterBySector(Locatable source,int sector){
        assert sector < Config.getInstance().getPart() && sector >= 0;
        this.source = source;
        this.sector = sector;
    }
    @Override
    public List<Locatable> filt() {
        return sMap.get(source).getStations(sector);
    }
}
