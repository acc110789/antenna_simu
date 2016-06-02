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
                    Config.getInstance().getMacCtsOrAckHeader(),
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

}
