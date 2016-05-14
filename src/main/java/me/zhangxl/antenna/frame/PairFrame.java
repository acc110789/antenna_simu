package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.PrecisionUtil;

/**
 * 这个桢由PCP节点在2.4GHz或者5GHz频率下发出,
 * 注意:当PCP节点发出信号的时候,此刻,其它节点都应该是
 * 闭嘴的状态,应该聆听PCP发出的信号。
 * PairFrame由PCP节点发出,用来表明PCP节点同意两个节点
 * 进行通信,PCP应当保证这两个节点的确是处于空闲的状态。
 * 同时PairFrame中还应该提供一个信道的编号,表明PCP同意
 * 的这两个通信节点应该在信道编号指定的信道里面进行信号
 * 的传输。PCP应该保证这个信道的确是空闲的,不然由可能造成
 * 冲撞。
 * PairFrame的大小,两个地址一共12个字节,再加上信道的编号
 * 两个字节(两个字节的编号最多可以标识65536个信道,实际上
 * 就算,60GHz附近有5GHz的信道谱资源,也基本不可能弄65536个信道出来
 * ,所以分配两个字节来标识所有的信道是绰绰有余的)。
 * Created by zhangxiaolong on 16/5/12.
 */
public class PairFrame extends Frame {

    private static final long frameLength = Config.getInstance().getPhyHeader() +//物理层的header
            Config.getInstance().getMacHeader() +//mac层的header
            2 * Config.getInstance().getAddrSize() +//两个地址
            2 * 8;//2个字节,表明信道的编号

    private static final double frameTimeLength = PrecisionUtil.div(frameLength,Config.getInstance().getBandWidth());
    private final int channel;

    public PairFrame(int srcId, int targetId, int fre, int channel) {
        super(srcId, targetId, frameLength, fre);
        this.channel = channel;
    }

    public int getChannel(){
        return this.channel;
    }

    public static double getFrameTimeLength(){
        return frameTimeLength;
    }

    @Override
    public double getNavDuration() {
        return 0;
    }
}
