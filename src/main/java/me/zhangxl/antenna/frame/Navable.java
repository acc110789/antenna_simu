package me.zhangxl.antenna.frame;

/**
 * Created by zhangxiaolong on 16/5/21.
 */
public interface Navable {
    /**
     * 收到与不是自己节点的Frame的时候,要设置NAV
     * NAV完毕之后,立马启动一个DIFS,DIFS之后则
     * 开始backOff
     * @return NAV持续的时间
     */
    double getNavDuration();
}
