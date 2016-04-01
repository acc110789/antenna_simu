package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.RtsFrame;

import java.util.List;

/**
 * Created by zhangxiaolong on 16/3/30.
 */
public interface MediumObserver {

    /**
     * 有多个Station正在发送RTS,碰撞
     * @param collisionFrames 碰撞的RtsFrame列表
     */
    void onRtsCollision(List<RtsFrame> collisionFrames);

    /**
     * 这个方法必须在Medium空闲DIFS之后才能回调
     * 当被这个方法调用的时候,正在进行backOff的
     * Station必须将backOff的count减少一
     */
    void onNewSLot();

    /**
     * 用于通知一个DIFS刚好已经完了,相关Station可以开始准备新的DataFrame了
     */
    void onPostDifs();
}
