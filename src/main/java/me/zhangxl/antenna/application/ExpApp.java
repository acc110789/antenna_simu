package me.zhangxl.antenna.application;

/**
 * 指数分布的app
 * Created by zhangxiaolong on 16/3/24.
 */
@Deprecated
public class ExpApp extends App {

    public ExpApp(int id) {
        super(id);
    }

    /**
     * 没有间隔
     * @return 安排下一个发送请求的时间点
     */
    @Override
    protected long getNextFrameInterval() {
        return 0;
    }

    /**
     * @return 下一个请求的长度
     */
    @Override
    protected long getNextFrameLength() {
        return 8000;
    }

    /**
     * 随机,均匀分布
     * @return 下一个请求的发送目标
     */
    @Override
    protected int getNextDesId() {
        return 0;
    }

}
