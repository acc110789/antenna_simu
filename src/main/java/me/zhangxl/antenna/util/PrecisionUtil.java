package me.zhangxl.antenna.util;

import java.math.BigDecimal;

/**
 * Created by zhangxiaolong on 16/4/20.
 */
public class PrecisionUtil {

    /**
     * 时间的精度(小数点之后)
     */
    private static final int timePrecision = 13;

    private static int getTimePrecision(){
        return timePrecision;
    }

    public static boolean equal(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.compareTo(b2) == 0;
    }

    public static double round(double v) {
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, getTimePrecision(), BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double add(double... values){
        BigDecimal result = new BigDecimal("0.0");
        for(double toAdd : values){
            BigDecimal aa = new BigDecimal(Double.toString(toAdd));
            result = result.add(aa);
        }
        return result.doubleValue();
    }

    public static double sub(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    public static boolean largeThan(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.compareTo(b2) > 0;
    }

    public static double mul(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    public static double div(double v1,double v2){
        return div(v1,v2,getTimePrecision());
    }

    private static double div(double v1, double v2, int scale){
        if(scale<0){
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
