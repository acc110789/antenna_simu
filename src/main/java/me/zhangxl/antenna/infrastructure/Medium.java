package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.request.Frame;

/**
 * 代表传输的介质(信道)
 * Created by zhangxiaolong on 16/3/24.
 */
public class Medium {
    
    private static final Medium sMedium = new Medium();

    public static Medium getInstance(){
        return sMedium;
    }
    
    private Medium(){
    }
    
    public void putRequest(Frame frame){
        // TODO: 16/3/25  将request发送给目标
    }
}
