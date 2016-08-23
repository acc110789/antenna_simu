package me.zhangxl.antenna.infrastructure.medium;

import java.util.List;

/**
 * Created by zhangxiaolong on 16/8/23.
 */
public interface Filter<T> {
    List<T> filt();
}
