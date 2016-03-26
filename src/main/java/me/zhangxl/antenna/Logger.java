package me.zhangxl.antenna;

/**
 * Created by zhangxiaolong on 16/3/25.
 */
public class Logger {

    private final String TAG;

    public Logger(Class clazz){
        TAG = clazz.getSimpleName();
    }

    // TODO: 16/3/25 declare a method which can print exception

    public void log(String info){
        System.out.println(TAG + "      " + info);
    }
}
