package me.zhangxl.antenna.util;

/**
 * Created by zhangxiaolong on 16/6/2.
 */
public class Constant {

    private static final double ctsTimeLength = PrecisionUtil.div(Config.getCtsLength(),
            Config.getBandWidth());
    /**
     * @return 仅仅是cts的那一小段对应的时间片,不算sifs
     */
    public static double getCtsTimeLength(){
        return ctsTimeLength;
    }

    private static final double ctsTimeout = PrecisionUtil.add(ctsTimeLength,
            Config.getDifs(), Config.getSifs());

    /**
     * @return 发送端发送完rts之后,最多等待cts的时间
     */
    public static double getCtsTimeOut(){
        return ctsTimeout;
    }

    private static long dataFrameLength = Config.getPayLoad()
            + Config.getPhyHeader()
            + Config.getMacHeader();

    public static long getDataFrameLength(){
        return dataFrameLength;
    }

    private static final double dataTimeLength = PrecisionUtil.div(
            getDataFrameLength(),
            Config.getBandWidth());
    /**
     * @return dataFrame那一小段对应的时间片
     */
    public static double getDataTimeLength(){
        return dataTimeLength;
    }

    private static final double dataTimeOut = PrecisionUtil.add(Config.getSifs(),
            Config.getDifs(), dataTimeLength);
    /**
     * @return 接收端发送完Cts之后,等待DataFrame的最长时间
     */
    public static double getDataTimeOut(){
        return dataTimeOut;
    }


    private static final double ackTimeLength = PrecisionUtil.div(Config.getAckLength(),
            Config.getBandWidth());
    /**
     * @return ackFrame那一小段对应的时间片
     */
    public static double getAckTimeLength(){
        return ackTimeLength;
    }

    private static final double ackTimeOut = PrecisionUtil.add(Config.getSifs(),
            Config.getDifs(), ackTimeLength);
    /**
     * @return 发送完DataFrame之后等待Ack的最长时间
     */
    public static double getAckTimeOut(){
        return ackTimeOut;
    }

    private static final double rtsTimeLength = PrecisionUtil.div(Config.getRtsLength(),
            Config.getBandWidth());
    /**
     * @return 同样是rtsFrame那一小段时间片的长度
     */
    public static double getRtsTimeLength(){
        return rtsTimeLength;
    }

    private static final double navFrameTimeLength = rtsTimeLength;

    public static double getNavFrameTimeLength(){
        return navFrameTimeLength;
    }

    /**
     * 一个字节的长度是8位
     */
    private static int byteLength = 8;
    /**
     * mac地址的长度,mac地址一共是6个字节,每个字节是8位,总共是64位
     */
    private static int addrSize = 6 * byteLength;
    private static int otcFrameLength = Config.getPhyHeader() +//物理层的header
            Config.getMacHeader() +//mac层的header
            2 * addrSize +//两个地址
            2 * byteLength; //用两个字节
    /**
     * @return 获取OtcFrame的帧长度
     */
    public static int getOtcFrameLength(){
        return otcFrameLength;
    }
    /**
     * @return OtcFrame那一小段时间片的长度
     */
    public static double getOtcFrameTimeLength(){
        return PrecisionUtil.div(getOtcFrameLength(), Config.getBandWidth());
    }

    private static final long bofFrameLength = Config.getPhyHeader() +
            Config.getMacHeader() +
            2 * byteLength;//2个字节表明下一轮最有竞争节点可能的退避窗口
    public static long getBofFrameLength(){
        return bofFrameLength;
    }

    private static final double bofFrameTimeLength = PrecisionUtil.div(getBofFrameLength(),
            Config.getBandWidth());
    public static double getBofFrameTimeLength(){
        return bofFrameTimeLength;
    }

    //没有一个所谓的rtsTimeOut

    //下面是各种nav的函数

    //收到一个不认识的ack是没有nav的,或者说ack的nav就是0吧

    private static final double dataNav = PrecisionUtil.add(Config.getSifs(),
            getAckTimeLength());
    /**
     * @return 收到一个不认识的DataFrame之后应该设置的nav长度
     */
    public static double getDataNav(){
        return dataNav;
    }

    private static final double ctsNav = PrecisionUtil.add(Config.getSifs(),
            getDataTimeLength(),getDataNav());
    /**
     * @return 收到不认识的cts之后应该设置的nav的长度
     */
    public static double getCtsNav(){
        return ctsNav;
    }

    private static final double rtsNav = PrecisionUtil.add(Config.getSifs(),
            getCtsTimeLength(),getCtsNav());

    /**
     * @return 收到不认识的rts之后应该设置的nav的长度
     */
    public static double getRtsNav(){
        return rtsNav;
    }

    public static double getPcpRtsTimeOut(int slots){
        return PrecisionUtil.add(getNormalCoolingDuration(),
                PrecisionUtil.mul(slots, Config.getSlotLength()),
                getRtsTimeOut());
    }

    private static double rtsTimeOut = PrecisionUtil.add(getRtsTimeLength(),
            Config.getSifs(),
            Config.getDifs());
    private static double getRtsTimeOut(){
        return rtsTimeOut;
    }

    /**
     * @param slots
     * @return 正常情况(不是超时)如果由rts发送过来就在这个时候准备处理了
     */
    public static double getPcpRtsDeadLine(int slots){
        return PrecisionUtil.add(getNormalCoolingDuration(),
                PrecisionUtil.mul(slots, Config.getSlotLength()),
                getRtsTimeLength());
    }

    public static double getNormalCoolingDuration() {
        return Config.getDifs();
    }

    private static double dataChannelDeadLine = PrecisionUtil.add(Config.getSifs(),
            getDataTimeLength(),
            Config.getSifs(),
            getAckTimeLength());
    public static double getDataChannelDeadLine() {
        return dataChannelDeadLine;
    }
}
