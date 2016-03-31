package me.zhangxl.antenna.util;

import java.io.PrintWriter;

/**
 * 打印一些log信息
 * Created by zhangxiaolong on 16/3/25.
 */
public class Logger {

    private boolean LOG = true;

    public static boolean DEBUG_CLOCK = true;

    public static boolean DEBUG_STATION = true;

    private static final PrintWriter printer = new PrintWriter(System.out,true);

    private final String TAG;

    public Logger(Class clazz){
        this(clazz,true);
    }

    public Logger(Class clazz,boolean needLog){
        TAG = clazz.getSimpleName();
        LOG = needLog;
    }

    public void log(Throwable e){
        logInner(e.toString());
    }

    public void log(String info){
        logInner(info);
    }

    public void log(String formater,Object... objects){
        logInner(formater,objects);
    }

    public void log(Throwable e,String info){
        logInner("%s \n %s",info,e.toString());
    }

    private void logInner(String formater,Object... objects){
        if(LOG) {
            printer.format(TAG + "  " + formater + "\n", objects);
        }
    }

}
