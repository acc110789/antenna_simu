package me.zhangxl.antenna.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by zhangxiaolong on 16/4/20.
 */
public class SimuLoggerManager {

    public static TimeLogger getLogger(String info){
        Logger logger = LogManager.getFormatterLogger(info);
        return new TimeLogger(logger,info);
    }

}
