package me.zhangxl.antenna;

import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Constant;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 测试获取Properties是否正确
 * Created by zhangxiaolong on 16/4/1.
 */
public class TestProperties {

    @Test
    public void properties() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("test_config.properties");
        Assert.assertNotNull(inputStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        properties.load(reader);
        for(String version : new String[]{"802.11b","802.11a","802.11g"}){
            for(String key : new String[]{"SLOT_LENGTH","SIFS","DEFAULT_CW"}){
                String realKey = version + key;
                float value = Float.valueOf(properties.getProperty(version + "_" + key));
                System.out.format("%.10f",value);
                System.out.println();
            }
        }
    }

    @Test
    public void testConfig(){
        System.out.println("rtslength " + Config.getInstance().getRtsLength());
        System.out.println("ctslength " + Config.getInstance().getCtsLength());
        System.out.println("ackLength "+ Config.getInstance().getAckLength());
        System.out.println("realDataLength " + Config.getInstance().getPayLoad());
        System.out.println("dataLength "+ Constant.getDataFrameLength());
    }
}
