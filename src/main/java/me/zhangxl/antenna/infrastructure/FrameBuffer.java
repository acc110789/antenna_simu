package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.base.Locatable;
import me.zhangxl.antenna.infrastructure.clock.TimeController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhangxiaolong on 16/8/24.
 */
public class FrameBuffer {
    private final List<Frame> buffer = new ArrayList<>();

    public void push(final Frame frame){
        buffer.add(frame);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                buffer.remove(frame);
            }
        },frame.getEndDuration());
    }

    public void pullAllOut(Locatable locatable){
        Iterator<Frame> iterator = buffer.iterator();
        while (iterator.hasNext()){
            Frame target = iterator.next();
            iterator.remove();
            locatable.beginReceiveFrame(target);
        }
    }
}
