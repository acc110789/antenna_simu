package me.zhangxl.antenna.util;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
    // TODO: 16/4/7 添加NAV 从RTS之后的sifs一直到ACK
    /**
     * 正常的流程如下
     * difs  -> ContentionWindow -> RTS -> sifs -> CTS -> sifs -> DATA -> sifs -> ACK -> difs -> ContentionWindow .........
     * 只考虑一种碰撞的情况如下:
     * difs  -> ContentionWindow -> RTS(碰撞) -> difs -> ContentionWindow -> ............
     */

    private float difs = -1;
    private float sifs = -1;
    private float slotLength = -1;
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
    private float bandWidth = -1;
    private float simulationDuration = -1;
    private float warmUp = -1;
    private long fixDataLength = -1;

    private Config() {
        try {
            loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Config getInstance() {
        return sInstance;
    }

    private void loadConfig() throws IOException {
        Properties properties = new Properties();
        Reader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(Config.class.
                    getClassLoader().getResourceAsStream("antenna_config.properties")));
            properties.load(reader);

            this.stationNum = Integer.valueOf(properties.getProperty("STATION_NUM"));
            this.maxCW = Integer.valueOf(properties.getProperty("MAX_CW"));

            String currentVersion = properties.getProperty("CURRENT_VERSION");
            this.slotLength = Float.valueOf(properties.getProperty(currentVersion + "_SLOT_LENGTH"));
            this.sifs = Float.valueOf(properties.getProperty(currentVersion + "_SIFS"));
            this.defaultCW = Integer.valueOf(properties.getProperty(currentVersion + "_DEFAULT_CW"));

            this.bandWidth = Float.valueOf(properties.getProperty("BAND_WIDTH"));
            this.phyHeader = Integer.valueOf(properties.getProperty("PHY_HEADER"));
            this.macHeader = Integer.valueOf(properties.getProperty("MAC_HEADER"));
            this.macRtsHeader = Integer.valueOf(properties.getProperty("MAC_RTS_HEADER"));

            this.simulationDuration = Float.valueOf(properties.getProperty("SIMULATION_DURATION"));
            this.warmUp = Float.valueOf(properties.getProperty("WARM_UP"));
            this.fixDataLength = Long.valueOf(properties.getProperty("FIX_DATA_LENGTH"));

            difs = sifs + 2 * slotLength;
            rtsLength = phyHeader + macRtsHeader;
            ctsLength = phyHeader + macHeader;
            ackLength = phyHeader + macHeader;
        } finally {
            IOUtils.closeQuietly(reader);
        }

    }

    public long getFixDataLength() {
        return this.fixDataLength;
    }

    public float getWarmUp() {
        return this.warmUp;
    }

    public int getStationNum() {
        return stationNum;
    }

    public float getDifs() {
        return difs;
    }

    public float getSifs() {
        return sifs;
    }

    public float getSlotLength() {
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

    public float getBandWidth() {
        return bandWidth;
    }

    public float getSimulationDuration() {
        return simulationDuration;
    }

}
