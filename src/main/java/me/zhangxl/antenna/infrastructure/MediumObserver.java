package me.zhangxl.antenna.infrastructure;

/**
 * Created by zhangxiaolong on 16/3/30.
 */
public interface MediumObserver {

    /**
     * 有多个Station正在发送RTS,碰撞
     */
    void onRtsCollision();

    /**
     * 这个方法必须在Medium空闲DIFS之后才能回调
     * 当被这个方法调用的时候,正在进行backOff的Station必须将backOff的count减少一
     */
    void onNewSLot();
}
