package me.zhangxl.antenna.infrastructure.pcp;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * Created by zhangxiaolong on 16/7/15.
 */
abstract class SaveItem {
    //item被摧毁的时间点
    final double endTimePoint;

    SaveItem(double endTimePoint){
        this.endTimePoint = endTimePoint;
    }

    /**
     * @return 还需要等待的时间
     */
    double getWaitingTime(){
        return PrecisionUtil.sub(this.endTimePoint, TimeController.getInstance().getCurrentTime());
    }

    abstract boolean hasId(int id);

    abstract int getPeerSize();
}
