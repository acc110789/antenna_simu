package me.zhangxl.antenna.infrastructure;

/**
 * 本来应该是每一个Station都有一个MediumObserver.
 * 但是本仿真程序假设已经做好了同步.
 * 因此所有的Station都用的是同一个MediumObserver.
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
