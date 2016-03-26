package me.zhangxl.antenna.application;

/**
 * 指数分布的app
 * Created by zhangxiaolong on 16/3/24.
 */
public class ExpApp extends App {

    public ExpApp(int id) {
        super(id);
    }

    /**
     * 安排下一个发送请求的时间点
     */
    @Override
    protected long getNextTimePoint() {
        // TODO: 16/3/24
        return 0;
    }

    /**
     * 下一个请求的长度
     */
    @Override
    protected long getNextDataRequestLength() {
        // TODO: 16/3/24
        return 0;
    }

    /**
     * 下一个请求的发送目标
     */
    @Override
    protected int getNextTargetId() {
        return 0;
    }

}
