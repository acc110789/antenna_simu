package me.zhangxl.antenna;

import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Created by zhangxiaolong on 16/3/25.
 */
public class Logger {

    public static boolean LOG_CLOCK = false;

    private final String TAG;

    private PrintWriter printer;

    public Logger(Class clazz){
        TAG = clazz.getSimpleName();
        this.printer = new PrintWriter(System.out);
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
        printer.format("%s:  " + formater,TAG, Arrays.toString(objects));
    }

}
