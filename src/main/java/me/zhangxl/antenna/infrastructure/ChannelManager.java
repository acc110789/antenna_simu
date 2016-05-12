package me.zhangxl.antenna.infrastructure;

import me.zhangxl.antenna.util.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 信道信息,管理着信道编号
 * Created by zhangxiaolong on 16/5/11.
 */
public class ChannelManager {

    private static ChannelManager sInstance = new ChannelManager();
    private final List<Integer> rtsChannels = new ArrayList<>();
    private Integer apChannel;
    private final List<Integer> dataChannels = new ArrayList<>();
    private final Random random = new Random(System.nanoTime()+566677776543L);

    public static ChannelManager getInstance() {
        return sInstance;
    }

    private ChannelManager() {
        checkDataValidity();
        allocateChannel();
    }

    private void allocateChannel() {
        int num = 0;
        for (int i = 0; i < Config.getInstance().getRtsFreCount(); i++) {
            rtsChannels.add(++num);
        }
        apChannel = ++num;
        for (int i = 0; i < Config.getInstance().getDataFreCount(); i++) {
            dataChannels.add(++num);
        }
    }

    private void checkDataValidity() {
        boolean valid = true;
        if (Config.getInstance().getDataFreCount() < 1) {
            valid = false;
        } else if (Config.getInstance().getRtsFreCount() < 1) {
            valid = false;
        }
        if (!valid) {
            throw new IllegalStateException();
        }
    }

    public int getPcpChannel(){
        return this.apChannel;
    }

    public int getRandomRtsChannel(){
        int randomIndex = random.nextInt(this.rtsChannels.size());
        return this.rtsChannels.get(randomIndex);
    }

    public List<Integer> getDataChannels(){
        return new ArrayList<>(dataChannels);
    }

    public boolean isApChannel(Integer apChannel){
        return apChannel.equals(this.apChannel);
    }

    public boolean isDataChannel(Integer dataChannel){
        return dataChannels.contains(dataChannel);
    }

    public boolean isRtsChannel(Integer rtsChannel){
        return rtsChannels.contains(rtsChannel);
    }
}
