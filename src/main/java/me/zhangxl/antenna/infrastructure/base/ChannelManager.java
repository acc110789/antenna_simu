package me.zhangxl.antenna.infrastructure.base;

import me.zhangxl.antenna.util.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 信道信息,管理着信道编号
 * Created by zhangxiaolong on 16/5/11.
 */
public class ChannelManager {

    private static final List<Integer> rtsChannels = new ArrayList<>();
    private static Integer apChannel;
    private static final List<Integer> dataChannels = new ArrayList<>();
    private static final Random random = new Random(System.nanoTime()+566677776543L);

    static {
        checkDataValidity();
        allocateChannel();
    }

    private static void allocateChannel() {
        int num = 0;
        for (int i = 0; i < Config.getRtsFreCount(); i++) {
            rtsChannels.add(++num);
        }
        apChannel = ++num;
        for (int i = 0; i < Config.getDataFreCount(); i++) {
            dataChannels.add(++num);
        }
    }

    private static void checkDataValidity() {
        boolean valid = true;
        if (Config.getDataFreCount() < 1) {
            valid = false;
        } else if (Config.getRtsFreCount() < 1) {
            valid = false;
        }
        if (!valid) {
            throw new IllegalStateException();
        }
    }

    public static int getPcpChannel(){
        return apChannel;
    }

    public static int getRandomRtsChannel(){
        int randomIndex = random.nextInt(rtsChannels.size());
        return rtsChannels.get(randomIndex);
    }

    public static List<Integer> getDataChannels(){
        return new ArrayList<>(dataChannels);
    }

    public static boolean isPcpChannel(Integer apChannel){
        return apChannel.equals(ChannelManager.apChannel);
    }

    public static boolean isDataChannel(Integer dataChannel){
        return dataChannels.contains(dataChannel);
    }

    public static boolean isRtsChannel(Integer rtsChannel){
        return rtsChannels.contains(rtsChannel);
    }

    public static boolean isOmniChannel(Integer channel){
        return isPcpChannel(channel);
    }

    public static boolean isDirectChannel(Integer channel){
        return isDataChannel(channel) || isRtsChannel(channel);
    }
}
