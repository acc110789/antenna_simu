package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.request.Frame;

/**
 * 代表传输的介质(信道)
 *
 * Created by zhangxiaolong on 16/3/24.
 */
class Medium {
    
    private static final Medium sMedium = new Medium();

    static Medium getInstance(){
        return sMedium;
    }

    // TODO: 16/3/30 Medium要负责在恰当的时间回调MediumObservers的两个方法
    private Medium(){
    }
    
    void putRequest(Frame frame){
        // TODO: 16/3/25  将request发送给目标
    }
}
