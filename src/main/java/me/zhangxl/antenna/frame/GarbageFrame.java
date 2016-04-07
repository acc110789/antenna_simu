package me.zhangxl.antenna.frame;

import java.util.ArrayList;
import java.util.List;

/**
 * GarbageFrame 垃圾Frame,GarbageFrame通常是由两个Frame叠加而成的
 * 比如两个RtsFrame同时发送就会出现冲突,Medium则不再发送两个RtsFrame
 * Medium将这个RtsFrame的叠加视为一个GarbageFrame.
 * Created by zhangxiaolong on 16/4/7.
 */
public class GarbageFrame extends Frame {

    private List<Integer> srcIds = new ArrayList<>();
    private List<Integer> targetIds = new ArrayList<>();

    GarbageFrame(List<Integer> srcIds,List<Integer> targetIds,long length) {
        super(-1, -1, length);
        this.srcIds = srcIds;
        this.targetIds = targetIds;
    }

}
