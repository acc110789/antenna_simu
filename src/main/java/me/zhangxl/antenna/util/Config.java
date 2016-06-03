package me.zhangxl.antenna.util;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Properties;

/**
 * 配置信息
 * Created by zhangxiaolong on 16/3/24.
 */
public class Config {

    public static final int DEFAULT_DATA_FRAME_START_TIME = -1;
    public static final int DEFAULT_DATA_FRAME_COLLISION_TIME = -1;
    private static final Config sInstance = new Config();
    //局域网中站点的数量
    private int stationNum = -1;
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

    private double difs = -1;
    private double sifs = -1;
    private double slotLength = -1;
    //contentionWindow,竞争窗口
    private int defaultCW = -1;
    private int maxCW = -1;
    //bits 物理层的长度
    private int phyHeader = -1;
    //ACK,CTS
    private int macHeader = -1;
    private int macRtsHeader = -1;
    private int rtsLength = -1;
    private int ctsLength = -1;
    private int ackLength = -1;
    private double bandWidth = -1;
    private double simulationDuration = -1;
    private double warmUp = -1;
    private long fixDataLength = -1;
    private double eifs= -1;
    private int antennaMode = -1;
    private int part = -1;

    private Config() {
        try {
//            loadConfigProperties();
            loadConfigJSON();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Config getInstance() {
        return sInstance;
    }

    @Deprecated
    private void loadConfigProperties() throws IOException {
        Properties properties = new Properties();
        Reader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(Config.class.
                    getClassLoader().getResourceAsStream("antenna_config.properties")));
            properties.load(reader);

            this.stationNum = Integer.valueOf(properties.getProperty("STATION_NUM"));
            this.maxCW = Integer.valueOf(properties.getProperty("MAX_CW"));

            String currentVersion = properties.getProperty("CURRENT_VERSION");
            this.slotLength = PrecisionUtil.round(Double.valueOf(properties.getProperty(currentVersion + "_SLOT_LENGTH")));
            this.sifs = PrecisionUtil.round(Double.valueOf(properties.getProperty(currentVersion + "_SIFS")));
            this.defaultCW = Integer.valueOf(properties.getProperty(currentVersion + "_DEFAULT_CW"));

            this.bandWidth = PrecisionUtil.round(Double.valueOf(properties.getProperty("BAND_WIDTH")));
            this.phyHeader = Integer.valueOf(properties.getProperty("PHY_HEADER"));
            this.macHeader = Integer.valueOf(properties.getProperty("MAC_HEADER"));
            this.macRtsHeader = Integer.valueOf(properties.getProperty("MAC_RTS_HEADER"));

            this.simulationDuration = PrecisionUtil.round(Double.valueOf(properties.getProperty("SIMULATION_DURATION")));
            this.warmUp = PrecisionUtil.round(Double.valueOf(properties.getProperty("WARM_UP")));
            this.fixDataLength = Long.valueOf(properties.getProperty("FIX_DATA_LENGTH"));

            difs = PrecisionUtil.add(PrecisionUtil.mul(2.0,slotLength),sifs);
            rtsLength = phyHeader + macRtsHeader;
            ctsLength = phyHeader + macHeader;
            ackLength = phyHeader + macHeader;
            eifs = PrecisionUtil.add(sifs , PrecisionUtil.div(ackLength,bandWidth) , difs);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private void loadConfigJSON() throws IOException {
        InputStream input = null;
        try {
            input = Config.class.getClassLoader().getResourceAsStream("antenna_config.json");
            JSONObject object = new JSONObject(new JSONTokener(input));

            this.stationNum = object.getInt("STATION_NUM");
            this.maxCW = object.getInt("MAX_CW");

            String currentVersion = object.getString("CURRENT_VERSION");
            JSONObject subObj = object.getJSONObject(currentVersion);

            this.slotLength = PrecisionUtil.round(subObj.getDouble("SLOT_LENGTH"));
            this.sifs = PrecisionUtil.round(subObj.getDouble("SIFS"));
            this.difs = PrecisionUtil.round(subObj.getDouble("DIFS"));
            this.defaultCW = subObj.getInt("DEFAULT_CW");

            this.bandWidth = PrecisionUtil.round(object.getDouble("BAND_WIDTH"));
            this.phyHeader = object.getInt("PHY_HEADER");
            this.macHeader = object.getInt("MAC_DATA_HEADER");
            this.macRtsHeader = object.getInt("MAC_RTS_HEADER");

            this.simulationDuration = PrecisionUtil.round(object.getDouble("SIMULATION_DURATION"));
            this.warmUp = PrecisionUtil.round(object.getDouble("WARM_UP"));
            this.fixDataLength = object.getLong("FIX_DATA_LENGTH");
            this.antennaMode = object.getInt("ANTENNA_MODE");
            //part的含义:具体将一个圆周分成多少份
            this.part = object.getInt("PART");

            rtsLength = phyHeader + macRtsHeader;
            ctsLength = phyHeader + object.getInt("MAC_CTS_HEADER");
            ackLength = phyHeader + object.getInt("MAC_ACK_HEADER");
            eifs = PrecisionUtil.add(sifs , PrecisionUtil.div(ackLength,bandWidth) , difs);
        } catch (JSONException e){
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(input);
        }

    }

    public long getFixDataLength() {
        return this.fixDataLength;
    }

    public double getWarmUp() {
        return this.warmUp;
    }

    public int getStationNum() {
        return stationNum;
    }

    public double getDifs() {
        return difs;
    }

    public double getEifs(){
        return this.eifs;
    }

    public double getSifs() {
        return sifs;
    }

    public double getSlotLength() {
        return slotLength;
    }

    public int getDefaultCW() {
        return defaultCW;
    }

    public int getMaxCW() {
        return maxCW;
    }

    public int getPhyHeader() {
        return phyHeader;
    }

    public int getMacHeader() {
        return macHeader;
    }

    public int getMacRtsHeader() {
        return macRtsHeader;
    }

    public int getRtsLength() {
        return rtsLength;
    }

    public int getCtsLength() {
        return ctsLength;
    }

    public int getAckLength() {
        return ackLength;
    }

    public double getBandWidth() {
        return bandWidth;
    }

    public double getSimulationDuration() {
        return simulationDuration;
    }

    public int getAntennaMode(){
        return this.antennaMode;
    }

    /**
     * @return 定向天线将一个圆周分成多少份
     */
    public int getPart(){
        return this.part;
    }

}
