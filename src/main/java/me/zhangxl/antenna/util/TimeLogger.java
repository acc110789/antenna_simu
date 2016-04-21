package me.zhangxl.antenna.util;

import me.zhangxl.antenna.infrastructure.clock.TimeController;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.util.Supplier;

/**
 * Created by zhangxiaolong on 16/4/20.
 */
public class TimeLogger implements Logger {

    private static final String sFormatter = "%s%s";
    public static boolean DEBUG_CLOCK = false;
    public static boolean DEBUG_STATION = false;
    public static boolean DEBUG_FRAME = true;
    private final Logger mLogger;
    private final String mTag;
    private boolean logHeader = true;
    public static final String headerFormatter = "%-20s%-12s";

    TimeLogger(Logger logger, String tag) {
        this.mLogger = logger;
        this.mTag = tag;
    }

    public void logHeader(){
        logHeader = true;
    }

    public void unLogHeader(){
        logHeader = false;
    }

    private String getHeader(){
        if(logHeader) {
            String time = String.format("%#.14f", TimeController.getInstance().getCurrentTime());
            return String.format(headerFormatter,time,mTag);
        } else {
            return "";
        }
    }

    public void ln(){
        System.out.println();
    }

    @Override
    public void catching(Level level, Throwable t) {
        mLogger.catching(level, t);
    }

    @Override
    public void catching(Throwable t) {
        mLogger.catching(t);
    }

    @Override
    public void debug(Marker marker, Message msg) {
        mLogger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, Message msg, Throwable t) {
        mLogger.debug(marker, msg, t);
    }

    @Override
    public void debug(Marker marker, MessageSupplier msgSupplier) {
        mLogger.debug(marker, msgSupplier);
    }

    @Override
    public void debug(Marker marker, MessageSupplier msgSupplier, Throwable t) {
        mLogger.debug(marker, msgSupplier, t);
    }

    @Override
    public void debug(Marker marker, Object message) {
        mLogger.debug(marker, message);
    }

    @Override
    public void debug(Marker marker, Object message, Throwable t) {
        mLogger.debug(marker, message, t);
    }

    @Override
    public void debug(Marker marker, String message) {
        mLogger.debug(marker, message);
    }

    @Override
    public void debug(Marker marker, String message, Object... params) {
        mLogger.debug(marker, message, params);
    }

    @Override
    public void debug(Marker marker, String message, Supplier<?>... paramSuppliers) {
        mLogger.debug(marker, message, paramSuppliers);
    }

    @Override
    public void debug(Marker marker, String message, Throwable t) {
        mLogger.debug(marker, message, t);
    }

    @Override
    public void debug(Marker marker, Supplier<?> msgSupplier) {
        mLogger.debug(marker, msgSupplier);
    }

    @Override
    public void debug(Marker marker, Supplier<?> msgSupplier, Throwable t) {
        mLogger.debug(marker, msgSupplier, t);
    }

    @Override
    public void debug(Message msg) {
        mLogger.debug(msg);
    }

    @Override
    public void debug(Message msg, Throwable t) {
        mLogger.debug(msg, t);
    }

    @Override
    public void debug(MessageSupplier msgSupplier) {
        mLogger.debug(msgSupplier);
    }

    @Override
    public void debug(MessageSupplier msgSupplier, Throwable t) {
        mLogger.debug(msgSupplier, t);
    }

    @Override
    public void debug(Object message) {
        mLogger.debug(message);
    }

    @Override
    public void debug(Object message, Throwable t) {
        mLogger.debug(message, t);
    }

    @Override
    public void debug(String message) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.debug(formatter);
    }

    @Override
    public void debug(String message, Object... params) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.debug(formatter, params);
    }

    @Override
    public void debug(String message, Supplier<?>... paramSuppliers) {
        mLogger.debug(message, paramSuppliers);
    }

    @Override
    public void debug(String message, Throwable t) {
        mLogger.debug(message, t);
    }

    @Override
    public void debug(Supplier<?> msgSupplier) {
        mLogger.debug(msgSupplier);
    }

    @Override
    public void debug(Supplier<?> msgSupplier, Throwable t) {
        mLogger.debug(msgSupplier, t);
    }

    @Override
    public void entry() {
        mLogger.entry();
    }

    @Override
    public void entry(Object... params) {
        mLogger.entry(params);
    }

    @Override
    public void error(Marker marker, Message msg) {
        mLogger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, Message msg, Throwable t) {
        mLogger.error(marker, msg, t);
    }

    @Override
    public void error(Marker marker, MessageSupplier msgSupplier) {
        mLogger.error(marker, msgSupplier);
    }

    @Override
    public void error(Marker marker, MessageSupplier msgSupplier, Throwable t) {
        mLogger.error(marker, msgSupplier, t);
    }

    @Override
    public void error(Marker marker, Object message) {
        mLogger.error(marker, message);
    }

    @Override
    public void error(Marker marker, Object message, Throwable t) {
        mLogger.error(marker, message, t);
    }

    @Override
    public void error(Marker marker, String message) {
        mLogger.error(marker, message);
    }

    @Override
    public void error(Marker marker, String message, Object... params) {
        mLogger.error(marker, message, params);
    }

    @Override
    public void error(Marker marker, String message, Supplier<?>... paramSuppliers) {
        mLogger.error(marker, message, paramSuppliers);
    }

    @Override
    public void error(Marker marker, String message, Throwable t) {
        mLogger.error(marker, message, t);
    }

    @Override
    public void error(Marker marker, Supplier<?> msgSupplier) {
        mLogger.error(marker, msgSupplier);
    }

    @Override
    public void error(Marker marker, Supplier<?> msgSupplier, Throwable t) {
        mLogger.error(marker, msgSupplier, t);
    }

    @Override
    public void error(Message msg) {
        mLogger.error(msg);
    }

    @Override
    public void error(Message msg, Throwable t) {
        mLogger.error(msg, t);
    }

    @Override
    public void error(MessageSupplier msgSupplier) {
        mLogger.error(msgSupplier);
    }

    @Override
    public void error(MessageSupplier msgSupplier, Throwable t) {
        mLogger.error(msgSupplier, t);
    }

    @Override
    public void error(Object message) {
        mLogger.error(message);
    }

    @Override
    public void error(Object message, Throwable t) {
        mLogger.error(message, t);
    }

    @Override
    public void error(String message) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.error(formatter);
    }

    @Override
    public void error(String message, Object... params) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.error(formatter, params);
    }

    @Override
    public void error(String message, Supplier<?>... paramSuppliers) {
        mLogger.error(message, paramSuppliers);
    }

    @Override
    public void error(String message, Throwable t) {
        mLogger.error(message, t);
    }

    @Override
    public void error(Supplier<?> msgSupplier) {
        mLogger.error(msgSupplier);
    }

    @Override
    public void error(Supplier<?> msgSupplier, Throwable t) {
        mLogger.error(msgSupplier, t);
    }

    @Override
    public void exit() {
        mLogger.exit();
    }

    @Override
    public <R> R exit(R result) {
        return mLogger.exit(result);
    }

    @Override
    public void fatal(Marker marker, Message msg) {
        mLogger.fatal(marker, msg);
    }

    @Override
    public void fatal(Marker marker, Message msg, Throwable t) {
        mLogger.fatal(marker, msg, t);
    }

    @Override
    public void fatal(Marker marker, MessageSupplier msgSupplier) {
        mLogger.fatal(marker, msgSupplier);
    }

    @Override
    public void fatal(Marker marker, MessageSupplier msgSupplier, Throwable t) {
        mLogger.fatal(marker, msgSupplier, t);
    }

    @Override
    public void fatal(Marker marker, Object message) {
        mLogger.fatal(marker, message);
    }

    @Override
    public void fatal(Marker marker, Object message, Throwable t) {
        mLogger.fatal(marker, message, t);
    }

    @Override
    public void fatal(Marker marker, String message) {
        mLogger.fatal(marker, message);
    }

    @Override
    public void fatal(Marker marker, String message, Object... params) {
        mLogger.fatal(marker, message, params);
    }

    @Override
    public void fatal(Marker marker, String message, Supplier<?>... paramSuppliers) {
        mLogger.fatal(marker, message, paramSuppliers);
    }

    @Override
    public void fatal(Marker marker, String message, Throwable t) {
        mLogger.fatal(marker, message, t);
    }

    @Override
    public void fatal(Marker marker, Supplier<?> msgSupplier) {
        mLogger.fatal(marker, msgSupplier);
    }

    @Override
    public void fatal(Marker marker, Supplier<?> msgSupplier, Throwable t) {
        mLogger.fatal(marker, msgSupplier, t);
    }

    @Override
    public void fatal(Message msg) {
        mLogger.fatal(msg);
    }

    @Override
    public void fatal(Message msg, Throwable t) {
        mLogger.fatal(msg, t);
    }

    @Override
    public void fatal(MessageSupplier msgSupplier) {
        mLogger.fatal(msgSupplier);
    }

    @Override
    public void fatal(MessageSupplier msgSupplier, Throwable t) {
        mLogger.fatal(msgSupplier, t);
    }

    @Override
    public void fatal(Object message) {
        mLogger.fatal(message);
    }

    @Override
    public void fatal(Object message, Throwable t) {
        mLogger.fatal(message, t);
    }

    @Override
    public void fatal(String message) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.fatal(formatter);
    }

    @Override
    public void fatal(String message, Object... params) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.fatal(formatter, params);
    }

    @Override
    public void fatal(String message, Supplier<?>... paramSuppliers) {
        mLogger.fatal(message, paramSuppliers);
    }

    @Override
    public void fatal(String message, Throwable t) {
        mLogger.fatal(message, t);
    }

    @Override
    public void fatal(Supplier<?> msgSupplier) {
        mLogger.fatal(msgSupplier);
    }

    @Override
    public void fatal(Supplier<?> msgSupplier, Throwable t) {
        mLogger.fatal(msgSupplier, t);
    }

    @Override
    public Level getLevel() {
        return mLogger.getLevel();
    }

    @Override
    public MessageFactory getMessageFactory() {
        return mLogger.getMessageFactory();
    }

    @Override
    public String getName() {
        return mLogger.getName();
    }

    @Override
    public void info(Marker marker, Message msg) {
        mLogger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, Message msg, Throwable t) {
        mLogger.info(marker, msg, t);
    }

    @Override
    public void info(Marker marker, MessageSupplier msgSupplier) {
        mLogger.info(marker, msgSupplier);
    }

    @Override
    public void info(Marker marker, MessageSupplier msgSupplier, Throwable t) {
        mLogger.info(marker, msgSupplier, t);
    }

    @Override
    public void info(Marker marker, Object message) {
        mLogger.info(marker, message);
    }

    @Override
    public void info(Marker marker, Object message, Throwable t) {
        mLogger.info(marker, message, t);
    }

    @Override
    public void info(Marker marker, String message) {
        mLogger.info(marker, message);
    }

    @Override
    public void info(Marker marker, String message, Object... params) {
        mLogger.info(marker, message, params);
    }

    @Override
    public void info(Marker marker, String message, Supplier<?>... paramSuppliers) {
        mLogger.info(marker, message, paramSuppliers);
    }

    @Override
    public void info(Marker marker, String message, Throwable t) {
        mLogger.info(marker, message, t);
    }

    @Override
    public void info(Marker marker, Supplier<?> msgSupplier) {
        mLogger.info(marker, msgSupplier);
    }

    @Override
    public void info(Marker marker, Supplier<?> msgSupplier, Throwable t) {
        mLogger.info(marker, msgSupplier, t);
    }

    @Override
    public void info(Message msg) {
        mLogger.info(msg);
    }

    @Override
    public void info(Message msg, Throwable t) {
        mLogger.info(msg, t);
    }

    @Override
    public void info(MessageSupplier msgSupplier) {
        mLogger.info(msgSupplier);
    }

    @Override
    public void info(MessageSupplier msgSupplier, Throwable t) {
        mLogger.info(msgSupplier, t);
    }

    @Override
    public void info(Object message) {
        mLogger.info(message);
    }

    @Override
    public void info(Object message, Throwable t) {
        mLogger.info(message, t);
    }

    @Override
    public void info(String message) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.info(formatter);
    }

    @Override
    public void info(String message, Object... params) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.info(formatter, params);
    }

    @Override
    public void info(String message, Supplier<?>... paramSuppliers) {
        mLogger.info(message, paramSuppliers);
    }

    @Override
    public void info(String message, Throwable t) {
        mLogger.info(message, t);
    }

    @Override
    public void info(Supplier<?> msgSupplier) {
        mLogger.info(msgSupplier);
    }

    @Override
    public void info(Supplier<?> msgSupplier, Throwable t) {
        mLogger.info(msgSupplier, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return mLogger.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return mLogger.isDebugEnabled(marker);
    }

    @Override
    public boolean isEnabled(Level level) {
        return mLogger.isEnabled(level);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker) {
        return mLogger.isEnabled(level, marker);
    }

    @Override
    public boolean isErrorEnabled() {
        return mLogger.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return mLogger.isErrorEnabled(marker);
    }

    @Override
    public boolean isFatalEnabled() {
        return mLogger.isFatalEnabled();
    }

    @Override
    public boolean isFatalEnabled(Marker marker) {
        return mLogger.isFatalEnabled(marker);
    }

    @Override
    public boolean isInfoEnabled() {
        return mLogger.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return mLogger.isInfoEnabled(marker);
    }

    @Override
    public boolean isTraceEnabled() {
        return mLogger.isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return mLogger.isTraceEnabled(marker);
    }

    @Override
    public boolean isWarnEnabled() {
        return mLogger.isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return mLogger.isWarnEnabled(marker);
    }

    @Override
    public void log(Level level, Marker marker, Message msg) {
        mLogger.log(level, marker, msg);
    }

    @Override
    public void log(Level level, Marker marker, Message msg, Throwable t) {
        mLogger.log(level, marker, msg, t);
    }

    @Override
    public void log(Level level, Marker marker, MessageSupplier msgSupplier) {
        mLogger.log(level, marker, msgSupplier);
    }

    @Override
    public void log(Level level, Marker marker, MessageSupplier msgSupplier, Throwable t) {
        mLogger.log(level, marker, msgSupplier, t);
    }

    @Override
    public void log(Level level, Marker marker, Object message) {
        mLogger.log(level, marker, message);
    }

    @Override
    public void log(Level level, Marker marker, Object message, Throwable t) {
        mLogger.log(level, marker, message, t);
    }

    @Override
    public void log(Level level, Marker marker, String message) {
        mLogger.log(level, marker, message);
    }

    @Override
    public void log(Level level, Marker marker, String message, Object... params) {
        mLogger.log(level, marker, message, params);
    }

    @Override
    public void log(Level level, Marker marker, String message, Supplier<?>... paramSuppliers) {
        mLogger.log(level, marker, message, paramSuppliers);
    }

    @Override
    public void log(Level level, Marker marker, String message, Throwable t) {
        mLogger.log(level, marker, message, t);
    }

    @Override
    public void log(Level level, Marker marker, Supplier<?> msgSupplier) {
        mLogger.log(level, marker, msgSupplier);
    }

    @Override
    public void log(Level level, Marker marker, Supplier<?> msgSupplier, Throwable t) {
        mLogger.log(level, marker, msgSupplier, t);
    }

    @Override
    public void log(Level level, Message msg) {
        mLogger.log(level, msg);
    }

    @Override
    public void log(Level level, Message msg, Throwable t) {
        mLogger.log(level, msg, t);
    }

    @Override
    public void log(Level level, MessageSupplier msgSupplier) {
        mLogger.log(level, msgSupplier);
    }

    @Override
    public void log(Level level, MessageSupplier msgSupplier, Throwable t) {
        mLogger.log(level, msgSupplier, t);
    }

    @Override
    public void log(Level level, Object message) {
        mLogger.log(level, message);
    }

    @Override
    public void log(Level level, Object message, Throwable t) {
        mLogger.log(level, message, t);
    }

    @Override
    public void log(Level level, String message) {
        mLogger.log(level, message);
    }

    @Override
    public void log(Level level, String message, Object... params) {
        mLogger.log(level, message, params);
    }

    @Override
    public void log(Level level, String message, Supplier<?>... paramSuppliers) {
        mLogger.log(level, message, paramSuppliers);
    }

    @Override
    public void log(Level level, String message, Throwable t) {
        mLogger.log(level, message, t);
    }

    @Override
    public void log(Level level, Supplier<?> msgSupplier) {
        mLogger.log(level, msgSupplier);
    }

    @Override
    public void log(Level level, Supplier<?> msgSupplier, Throwable t) {
        mLogger.log(level, msgSupplier, t);
    }

    @Override
    public void printf(Level level, Marker marker, String format, Object... params) {
        mLogger.printf(level, marker, format, params);
    }

    @Override
    public void printf(Level level, String format, Object... params) {
        mLogger.printf(level, format, params);
    }

    @Override
    public <T extends Throwable> T throwing(Level level, T t) {
        return mLogger.throwing(level, t);
    }

    @Override
    public <T extends Throwable> T throwing(T t) {
        return mLogger.throwing(t);
    }

    @Override
    public void trace(Marker marker, Message msg) {
        mLogger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, Message msg, Throwable t) {
        mLogger.trace(marker, msg, t);
    }

    @Override
    public void trace(Marker marker, MessageSupplier msgSupplier) {
        mLogger.trace(marker, msgSupplier);
    }

    @Override
    public void trace(Marker marker, MessageSupplier msgSupplier, Throwable t) {
        mLogger.trace(marker, msgSupplier, t);
    }

    @Override
    public void trace(Marker marker, Object message) {
        mLogger.trace(marker, message);
    }

    @Override
    public void trace(Marker marker, Object message, Throwable t) {
        mLogger.trace(marker, message, t);
    }

    @Override
    public void trace(Marker marker, String message) {
        mLogger.trace(marker, message);
    }

    @Override
    public void trace(Marker marker, String message, Object... params) {
        mLogger.trace(marker, message, params);
    }

    @Override
    public void trace(Marker marker, String message, Supplier<?>... paramSuppliers) {
        mLogger.trace(marker, message, paramSuppliers);
    }

    @Override
    public void trace(Marker marker, String message, Throwable t) {
        mLogger.trace(marker, message, t);
    }

    @Override
    public void trace(Marker marker, Supplier<?> msgSupplier) {
        mLogger.trace(marker, msgSupplier);
    }

    @Override
    public void trace(Marker marker, Supplier<?> msgSupplier, Throwable t) {
        mLogger.trace(marker, msgSupplier, t);
    }

    @Override
    public void trace(Message msg) {
        mLogger.trace(msg);
    }

    @Override
    public void trace(Message msg, Throwable t) {
        mLogger.trace(msg, t);
    }

    @Override
    public void trace(MessageSupplier msgSupplier) {
        mLogger.trace(msgSupplier);
    }

    @Override
    public void trace(MessageSupplier msgSupplier, Throwable t) {
        mLogger.trace(msgSupplier, t);
    }

    @Override
    public void trace(Object message) {
        mLogger.trace(message);
    }

    @Override
    public void trace(Object message, Throwable t) {
        mLogger.trace(message, t);
    }

    @Override
    public void trace(String message) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.trace(formatter);
    }

    @Override
    public void trace(String message, Object... params) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.trace(formatter, params);
    }

    @Override
    public void trace(String message, Supplier<?>... paramSuppliers) {
        mLogger.trace(message, paramSuppliers);
    }

    @Override
    public void trace(String message, Throwable t) {
        mLogger.trace(message, t);
    }

    @Override
    public void trace(Supplier<?> msgSupplier) {
        mLogger.trace(msgSupplier);
    }

    @Override
    public void trace(Supplier<?> msgSupplier, Throwable t) {
        mLogger.trace(msgSupplier, t);
    }

    @Override
    public void warn(Marker marker, Message msg) {
        mLogger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, Message msg, Throwable t) {
        mLogger.warn(marker, msg, t);
    }

    @Override
    public void warn(Marker marker, MessageSupplier msgSupplier) {
        mLogger.warn(marker, msgSupplier);
    }

    @Override
    public void warn(Marker marker, MessageSupplier msgSupplier, Throwable t) {
        mLogger.warn(marker, msgSupplier, t);
    }

    @Override
    public void warn(Marker marker, Object message) {
        mLogger.warn(marker, message);
    }

    @Override
    public void warn(Marker marker, Object message, Throwable t) {
        mLogger.warn(marker, message, t);
    }

    @Override
    public void warn(Marker marker, String message) {
        mLogger.warn(marker, message);
    }

    @Override
    public void warn(Marker marker, String message, Object... params) {
        mLogger.warn(marker, message, params);
    }

    @Override
    public void warn(Marker marker, String message, Supplier<?>... paramSuppliers) {
        mLogger.warn(marker, message, paramSuppliers);
    }

    @Override
    public void warn(Marker marker, String message, Throwable t) {
        mLogger.warn(marker, message, t);
    }

    @Override
    public void warn(Marker marker, Supplier<?> msgSupplier) {
        mLogger.warn(marker, msgSupplier);
    }

    @Override
    public void warn(Marker marker, Supplier<?> msgSupplier, Throwable t) {
        mLogger.warn(marker, msgSupplier, t);
    }

    @Override
    public void warn(Message msg) {
        mLogger.warn(msg);
    }

    @Override
    public void warn(Message msg, Throwable t) {
        mLogger.warn(msg, t);
    }

    @Override
    public void warn(MessageSupplier msgSupplier) {
        mLogger.warn(msgSupplier);
    }

    @Override
    public void warn(MessageSupplier msgSupplier, Throwable t) {
        mLogger.warn(msgSupplier, t);
    }

    @Override
    public void warn(Object message) {
        mLogger.warn(message);
    }

    @Override
    public void warn(Object message, Throwable t) {
        mLogger.warn(message, t);
    }

    @Override
    public void warn(String message) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.warn(formatter);
    }

    @Override
    public void warn(String message, Object... params) {
        String formatter = String.format(sFormatter,getHeader(),message);
        mLogger.warn(formatter, params);
    }

    @Override
    public void warn(String message, Supplier<?>... paramSuppliers) {
        mLogger.warn(message, paramSuppliers);
    }

    @Override
    public void warn(String message, Throwable t) {
        mLogger.warn(message, t);
    }

    @Override
    public void warn(Supplier<?> msgSupplier) {
        mLogger.warn(msgSupplier);
    }

    @Override
    public void warn(Supplier<?> msgSupplier, Throwable t) {
        mLogger.warn(msgSupplier, t);
    }
}
