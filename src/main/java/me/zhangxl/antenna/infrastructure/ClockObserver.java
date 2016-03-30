package me.zhangxl.antenna.infrastructure;

/**
 * Created by zhangxiaolong on 16/3/29.
 */
public interface ClockObserver {

    /**
     * 一个新的slot开始了
     */
    void onNewSlot();
}
