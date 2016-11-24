package me.zhangxl.antenna.util;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;

/**
 * 配置信息
 * Created by zhangxiaolong on 16/3/24.
 */
public class Config {

    public static final int DEFAULT_DATA_FRAME_START_TIME = -1;
    public static final int DEFAULT_DATA_FRAME_COLLISION_TIME = -1;
    //局域网中站点的数量
    private static int stationNum = -1;
    /**
     * 正常的流程如下
     * difs  -> ContentionWindow -> RTS -> sifs -> CTS -> sifs -> DATA -> sifs -> ACK -> difs -> ContentionWindow .........
     * 全向天线只考虑一种碰撞的情况如下:
     * difs  -> ContentionWindow -> RTS(碰撞) -> difs -> ContentionWindow -> ............
     * 对于定向天线来说:
     * 正常情况下,对于发送方来讲,任何一个环节出了问题,都认为是碰撞次数加一,比如发送方没有收到CTS,或者发送方没有收到ACK
     * 超时:RTS 发送完毕之后 CTS,ACK
     * CTS,ACK timeout = SIFS + DIFS + ACK/CTS,
     * 碰撞:没有发出去RTS
     * RTS(2) + DIFS
     *
     */
    private static boolean pcpMode = false;
    private static double difs = -1;
    private static double sifs = -1;
    private static double slotLength = -1;
    //contentionWindow,竞争窗口
    private static int defaultCW = -1;
    private static int maxCW = -1;
    //bits 物理层的长度
    private static int phyHeader = -1;
    //ACK,CTS
    private static int macHeader = -1;
    private static int rtsLength = -1;
    private static int ctsLength = -1;
    private static int ackLength = -1;
    private static double bandWidth = -1;
    private static double simulationDuration = -1;
    private static double warmUp = -1;
    private static long payLoad = -1;
    private static double eifs= -1;
    private static int antennaMode = -1;
    private static int part = -1;
    private static int rtsFreCount = -1;
    private static int dataFreCount = -1;

    static {
        try {
//            loadConfigProperties();
            loadConfigJSON();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadConfigJSON() throws IOException {
        InputStream input = null;
        try {
            input = Config.class.getClassLoader().getResourceAsStream("antenna_config.json");
            JSONObject object = new JSONObject(new JSONTokener(input));

            pcpMode = object.getBoolean("PCP_MODE");
            stationNum = object.getInt("STATION_NUM");
            maxCW = object.getInt("MAX_CW");

            String currentVersion = object.getString("CURRENT_VERSION");
            JSONObject subObj = object.getJSONObject(currentVersion);

            slotLength = PrecisionUtil.round(subObj.getDouble("SLOT_LENGTH"));
            sifs = PrecisionUtil.round(subObj.getDouble("SIFS"));
            difs = PrecisionUtil.round(subObj.getDouble("DIFS"));
            defaultCW = subObj.getInt("DEFAULT_CW");

            bandWidth = PrecisionUtil.round(object.getDouble("BAND_WIDTH"));
            phyHeader = object.getInt("PHY_HEADER");
            macHeader = object.getInt("MAC_DATA_HEADER");

            simulationDuration = PrecisionUtil.round(object.getDouble("SIMULATION_DURATION"));
            warmUp = PrecisionUtil.round(object.getDouble("WARM_UP"));
            payLoad = object.getLong("PAYLOAD");
            antennaMode = object.getInt("ANTENNA_MODE");
            //part的含义:具体将一个圆周分成多少份
            part = object.getInt("PART");
            //分配给RTS的频率的频率集的元素数量
            rtsFreCount = object.getInt("RTS_FRE_COUNT");
            //分配给DATA的频率的频率集的元素数量
            dataFreCount = object.getInt("DATA_FRE_COUNT");

            rtsLength = phyHeader + object.getInt("MAC_RTS_HEADER");
            ctsLength = phyHeader + object.getInt("MAC_CTS_HEADER");
            ackLength = phyHeader + object.getInt("MAC_ACK_HEADER");
            eifs = PrecisionUtil.add(sifs , PrecisionUtil.div(ackLength,bandWidth) , difs);
        } catch (JSONException e){
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(input);
        }

    }

    public static long getPayLoad() {
        return payLoad;
    }

    public static int getRtsFreCount(){
        return rtsFreCount;
    }

    public static int getDataFreCount(){
        return dataFreCount;
    }

    public static double getWarmUp() {
        return warmUp;
    }

    public static int getStationNum() {
        return stationNum;
    }

    public static double getDifs() {
        return difs;
    }

    public static double getEifs(){
        return eifs;
    }

    public static double getSifs() {
        return sifs;
    }

    public static double getSlotLength() {
        return slotLength;
    }

    public static int getDefaultCW() {
        return defaultCW;
    }

    public static int getMaxCW() {
        return maxCW;
    }

    public static int getPhyHeader() {
        return phyHeader;
    }

    public static int getMacHeader() {
        return macHeader;
    }

    public static int getRtsLength() {
        return rtsLength;
    }

    public static int getCtsLength() {
        return ctsLength;
    }

    public static int getAckLength() {
        return ackLength;
    }

    public static double getBandWidth() {
        return bandWidth;
    }

    public static double getSimulationDuration() {
        return simulationDuration;
    }

    public static int getAntennaMode(){
        return antennaMode;
    }

    /**
     * @return 定向天线将一个圆周分成多少份
     */
    public static int getPart(){
        return part;
    }

    public static boolean isPcpMode(){
        return pcpMode;
    }
}
