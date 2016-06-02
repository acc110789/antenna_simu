package me.zhangxl.antenna.infrastructure.frame_process;

import me.zhangxl.antenna.frame.Frame;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
interface Processor {
    void process(Frame frame);
}
