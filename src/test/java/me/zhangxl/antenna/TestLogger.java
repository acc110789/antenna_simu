package me.zhangxl.antenna;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Created by zhangxiaolong on 16/4/20.
 */
public class TestLogger {

    private static final Logger logger = LogManager.getFormatterLogger(TestLogger.class.getSimpleName());
    @Test
    public void testLogInfo(){
        logger.trace("yes it is");
        logger.debug("%d 122 %s",12,"kkk");
        logger.warn("1123333");
        logger.error("45555");
    }

}
