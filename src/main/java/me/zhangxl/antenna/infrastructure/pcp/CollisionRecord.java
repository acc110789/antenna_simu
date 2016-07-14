package me.zhangxl.antenna.infrastructure.pcp;

import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangxiaolong on 16/7/14.
 */
class CollisionRecord {
    private static Logger logger = SimuLoggerManager.getLogger("co_recorder");
    private boolean collisionInThisRound = false;
    private boolean severe = false;

    boolean collision(){
        return this.collisionInThisRound;
    }

    boolean severeCollision(){
        return collisionInThisRound && severe;
    }

    void analyze(List<RtsFrame> rtss){
        reset();
        List<Integer> dirtyChannels = new ArrayList<>();
        for (RtsFrame rts : rtss) {
            if (rts.isDirty()) {
                if (!collisionInThisRound) {
                    collisionInThisRound = true;
                }
                dirtyChannels.add(rts.getFre());
            }
        }
        if (collisionInThisRound) {
            logger.info("collision happen in this round");
        }
        //如果有超过70%的信道数量
        if(PrecisionUtil.div(dirtyChannels.size(), Config.getInstance().getRtsFreCount()) > 0.7){
            severe = true;
        }
    }

    private void reset(){
        this.collisionInThisRound = false;
        this.severe = false;
    }
}
