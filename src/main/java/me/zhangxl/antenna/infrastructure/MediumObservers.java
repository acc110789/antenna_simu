package me.zhangxl.antenna.infrastructure;

/**
 * 本来应该是每一个Station都有一个MediumObserver.
 * 但是本仿真程序假设已经做好了同步.
 * 因此所有的Station都用的是同一个MediumObserver.
 * MediumObserver 仅仅被Station持有.
 *
 * MediumObserver 需要做的事情:
 * 通知相应的Station将当前的DataFrame的backOff计数器减1
 *
 * 问题:谁来通知MediumObserver呢?
 *  {@link Medium}
 * Created by zhangxiaolong on 16/3/30.
 */
public class MediumObservers {
    private static MediumObservers ourInstance = new MediumObservers();

    public static MediumObservers getInstance() {
        return ourInstance;
    }

    private MediumObservers() {
    }
}
