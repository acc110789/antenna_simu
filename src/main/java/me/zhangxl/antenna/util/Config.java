package me.zhangxl.antenna.util;

/**
 * Created by zhangxiaolong on 16/3/24.
 */
public class Config {

    //局域网中站点的数量
    public static final int UserNum = 10;

    public static final long tifDuration = 200;

    public static final int DEFAULT_DATA_FRAME_START_TIME = -1;
    public static final int DEFAULT_DATA_FRAME_COLLISION_TIME = -1;

    /**
     * 正常的流程如下:
     * DIFS  -> ContentionWindow -> RTS -> SIFS -> CTS -> SIFS -> DATA -> SIFS -> ACK -> DIFS -> ContentionWindow .........
     * 只考虑一种碰撞的情况如下:
     * DIFS  -> ContentionWindow -> RTS(碰撞) -> DIFS -> ContentionWindow -> ............
     */

    public static float DIFS = 0;

    public static float SIFS = 0;

    public static float SLOT_LENGTH = 0;

    //contentionWindow,竞争窗口
    public static int DEFAULT_CW = 0;

    public static int MAX_CW = 10;

    //不同的802.11版本有的参数可能不一样
    private static final int vertionB = 1;
    private static final int vertionA = 2;
    private static final int vertionG = 3;

    private static final int currentVertion = 1;

    //bits 物理层的长度
    private static final int PHY_HEADER = 192;

    //ACK,CTS
    private static final int MAC_HEADER = 112;

    private static final int MAC_RTS_HEADER = 190;

    public static final int RTS_LENGTH = PHY_HEADER + MAC_RTS_HEADER;

    public static final int CTS_LENGTH = PHY_HEADER + MAC_HEADER;

    public static final int ACK_LENGTH = PHY_HEADER + MAC_HEADER;

    public static final float BAND_WIDTH = 1024 * 1024; //   rate:1Mb/s

    static {
        //time unit μs
        if (currentVertion == vertionB) {
            SLOT_LENGTH = (float) 20e-6;
            SIFS = (float) 10e-6;
            DEFAULT_CW = 5;
        } else if (currentVertion == vertionA) {
            SLOT_LENGTH = (float)9e-6;
            SIFS = (float)16e-6;
            DEFAULT_CW = 4;
        } else if (currentVertion == vertionG) {
            SLOT_LENGTH = (float)9e-6;
            SIFS = (float)16e-6;
            DEFAULT_CW = 4;
        } else {
            throw new IllegalArgumentException("no proper 802.11 vertion");
        }
        DIFS = SIFS + 2 * SLOT_LENGTH;

        DEFAULT_CW = 1;
    }

}
