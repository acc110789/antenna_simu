package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.clock.TimeController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用于将Frame缓存起来,比如在有些时候,Station出于nav或者写的状态
 * 不能立刻读取突然来的frame,则暂时将frame缓存起来
 * Created by zhangxiaolong on 16/8/24.
 */
public class FrameBuffer<T extends Frame> {
    private final List<T> cachedFrames = new ArrayList<>();

    public void push(final T frame){
        cachedFrames.add(frame);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                cachedFrames.remove(frame);
            }
        },frame.getEndDuration());
    }

    public void pullAllOut(Locatable locatable){
        Iterator<T> iterator = cachedFrames.iterator();
        while (iterator.hasNext()){
            T target = iterator.next();
            iterator.remove();
            locatable.beginReceiveFrame(target);
        }
    }
}
