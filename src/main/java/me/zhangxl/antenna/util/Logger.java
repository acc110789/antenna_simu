package me.zhangxl.antenna.util;

import java.io.PrintWriter;

/**
 * Created by zhangxiaolong on 16/3/25.
 */
public class Logger {

    public static boolean LOG_CLOCK = true;

    private static final PrintWriter printer = new PrintWriter(System.out,true);

    private final String TAG;

    public Logger(Class clazz){
        TAG = clazz.getSimpleName();
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
        printer.format(TAG + "  " + formater + "\n", objects);
    }

}
