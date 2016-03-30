package me.zhangxl.antenna;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class Config {

    //局域网中站点的数量
    public static final int UserNum = 10;

    public static final long tifDuration = 200;

    /**
     * 正常的流程如下:
     *  DIFS  -> ContentionWindow -> RTS -> SIFS -> CTS -> SIFS -> DATA -> SIFS -> ACK -> DIFS -> ContentionWindow .........
     *  只考虑一种碰撞的情况如下:
     *  DIFS  -> ContentionWindow -> RTS(碰撞) -> DIFS -> ContentionWindow -> ............
     * */

    public static  long DIFS = 0;

    public static  long SIFS = 0;

    public static long SLOT_LENGTH = 0;

    //contentionWindow,竞争窗口
    public static int CW = 0;

    public static final long EIFS = 0;

    //不同的802.11版本有的参数可能不一样
    public static final int vertionB = 1;
    public static final int vertionA = 2;
    public static final int vertionG = 3;

    public static final int currentVertion = vertionB;

    //bits 物理层的长度
    private static final int PHY_HEADER = 192;

    //ACK,CTS
    private static final int MAC_HEADER = 112;

    private static final int MAC_RTS_HEADER = 190;

    static {
        //time unit μs
        if(currentVertion == vertionB){
            SLOT_LENGTH = 20;
            SIFS = 10;
            CW = 31;
        } else if(currentVertion == vertionA){
            SLOT_LENGTH = 9;
            SIFS = 16;
            CW = 15;
        } else if(currentVertion == vertionG){
            SLOT_LENGTH = 9;
            SIFS = 16;
            CW = 15;
        } else {
            throw new IllegalArgumentException("no proper 802.11 vertion");
        }
        DIFS = SIFS + 2 * SLOT_LENGTH;
    }

}
