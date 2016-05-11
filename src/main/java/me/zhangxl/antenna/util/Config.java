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
    private boolean pcpMode = false;
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
    private int rtsLength = -1;
    private int ctsLength = -1;
    private int ackLength = -1;
    private double bandWidth = -1;
    private double simulationDuration = -1;
    private double warmUp = -1;
    private long payLoad = -1;
    private double eifs= -1;
    private int antennaMode = -1;
    private int part = -1;
    private int rtsFreCount = -1;
    private int dataFreCount = -1;

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

    private void loadConfigJSON() throws IOException {
        InputStream input = null;
        try {
            input = Config.class.getClassLoader().getResourceAsStream("antenna_config.json");
            JSONObject object = new JSONObject(new JSONTokener(input));

            this.pcpMode = object.getBoolean("PCP_MODE");
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

            this.simulationDuration = PrecisionUtil.round(object.getDouble("SIMULATION_DURATION"));
            this.warmUp = PrecisionUtil.round(object.getDouble("WARM_UP"));
            this.payLoad = object.getLong("PAYLOAD");
            this.antennaMode = object.getInt("ANTENNA_MODE");
            //part的含义:具体将一个圆周分成多少份
            this.part = object.getInt("PART");
            //分配给RTS的频率的频率集的元素数量
            this.rtsFreCount = object.getInt("RTS_FRE_COUNT");
            //分配给DATA的频率的频率集的元素数量
            this.dataFreCount = object.getInt("DATA_FRE_COUNT");

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

    public long getPayLoad() {
        return this.payLoad;
    }

    public int getRtsFreCount(){
        return this.rtsFreCount;
    }

    public int getDataFreCount(){
        return this.dataFreCount;
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

    public boolean isPcpMode(){
        return this.pcpMode;
    }
}
