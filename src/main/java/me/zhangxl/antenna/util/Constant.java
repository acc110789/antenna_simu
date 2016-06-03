package me.zhangxl.antenna.util;

/**
 * Created by zhangxiaolong on 16/6/2.
 */
public class Constant {

    private static final double ctsTimeLength = PrecisionUtil.div(Config.getInstance().getCtsLength(),
            Config.getInstance().getBandWidth());
    /**
     * @return 仅仅是cts的那一小段对应的时间片,不算sifs
     */
    public static double getCtsTimeLength(){
        return ctsTimeLength;
    }

    private static final double ctsTimeout = PrecisionUtil.add(ctsTimeLength,
            Config.getInstance().getDifs(),Config.getInstance().getSifs());

    /**
     * @return 发送端发送完rts之后,最多等待cts的时间
     */
    public static double getCtsTimeOut(){
        return ctsTimeout;
    }


    private static final double dataTimeLength = PrecisionUtil.div(
            Config.getInstance().getFixDataLength() +
                    Config.getInstance().getPhyHeader() +
                    Config.getInstance().getMacHeader(),
            Config.getInstance().getBandWidth());
    /**
     * @return dataFrame那一小段对应的时间片
     */
    public static double getDataTimeLength(){
        return dataTimeLength;
    }

    private static final double dataTimeOut = PrecisionUtil.add(Config.getInstance().getSifs(),
            Config.getInstance().getDifs(), dataTimeLength);
    /**
     * @return 接收端发送完Cts之后,等待DataFrame的最长时间
     */
    public static double getDataTimeOut(){
        return dataTimeOut;
    }


    private static final double ackTimeLength = PrecisionUtil.div(Config.getInstance().getAckLength(),
            Config.getInstance().getBandWidth());
    /**
     * @return ackFrame那一小段对应的时间片
     */
    public static double getAckTimeLength(){
        return ackTimeLength;
    }

    private static final double ackTimeOut = PrecisionUtil.add(Config.getInstance().getSifs(),
            Config.getInstance().getDifs(), ackTimeLength);
    /**
     * @return 发送完DataFrame之后等待Ack的最长时间
     */
    public static double getAckTimeOut(){
        return ackTimeOut;
    }

    private static final double rtsTimeLength = PrecisionUtil.div(Config.getInstance().getRtsLength(),
            Config.getInstance().getBandWidth());
    /**
     * @return 同样是rtsFrame那一小段时间片的长度
     */
    public static double getRtsTimeLength(){
        return rtsTimeLength;
    }

    //没有一个所谓的rtsTimeOut

    //下面是各种nav的函数

    //收到一个不认识的ack是没有nav的,或者说ack的nav就是0吧

    private static final double dataNav = PrecisionUtil.add(Config.getInstance().getSifs(),
            getAckTimeLength());
    /**
     * @return 收到一个不认识的DataFrame之后应该设置的nav长度
     */
    public static double getDataNav(){
        return dataNav;
    }

    private static final double ctsNav = PrecisionUtil.add(Config.getInstance().getSifs(),
            getDataTimeLength(),getDataNav());
    /**
     * @return 收到不认识的cts之后应该设置的nav的长度
     */
    public static double getCtsNav(){
        return ctsNav;
    }

    private static final double rtsNav = PrecisionUtil.add(Config.getInstance().getSifs(),
            getCtsTimeLength(),getCtsNav());

    /**
     * @return 收到不认识的rts之后应该设置的nav的长度
     */
    public static double getRtsNav(){
        return rtsNav;
    }

}
