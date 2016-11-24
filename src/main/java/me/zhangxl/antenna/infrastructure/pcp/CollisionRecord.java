package me.zhangxl.antenna.infrastructure.pcp;

import me.zhangxl.antenna.frame.RtsFrame;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;
import me.zhangxl.antenna.util.SimuLoggerManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by zhangxiaolong on 16/7/14.
 */
class CollisionRecord {
    private static Logger logger = SimuLoggerManager.getLogger("co_recorder");
    //在本轮中是否有某一个收到的RtsFrame是脏的
    private boolean collisionInThisRound = false;

    /**
    * 当碰撞的信道数量占超过了
    * {@link CollisionRecord#severeIndex}
    * */
    private boolean severeCollision = false;
    //碰撞是否严重的指标
    private static final double severeIndex = 0.7;

    boolean collision(){
        return this.collisionInThisRound;
    }

    boolean severeCollision(){
        return collisionInThisRound && severeCollision;
    }

    void analyze(List<RtsFrame> rtss){
        reset();
        Collection<Integer> dirtyChannels = new HashSet<>();
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
        if(PrecisionUtil.div(dirtyChannels.size(), Config.getRtsFreCount()) > 0.7){
            severeCollision = true;
        }
    }

    private void reset(){
        this.collisionInThisRound = false;
        this.severeCollision = false;
    }
}
