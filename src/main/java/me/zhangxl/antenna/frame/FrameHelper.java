package me.zhangxl.antenna.frame;

import me.zhangxl.antenna.infrastructure.base.ChannelManager;

/**
 * Created by zhangxiaolong on 16/6/14.
 */
public class FrameHelper {

    public static RtsFrame generateRtsFrame(Frame srcFrame){
        return new RtsFrame(srcFrame.getSrcId(),srcFrame.getTargetId(),
                ChannelManager.getInstance().getRandomRtsChannel());
    }
}
