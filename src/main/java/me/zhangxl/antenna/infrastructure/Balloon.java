package me.zhangxl.antenna.infrastructure;

/**
 * 用气球来表明是否在一段连续的时间内一直保持空闲
 * Created by zhangxiaolong on 16/4/18.
 */
public class Balloon {
    private boolean idle = true;

    public void reset(){
        idle = true;
    }

    public void touch(){
        idle = false;
    }

    public boolean isIdle(){
        return idle;
    }
}
