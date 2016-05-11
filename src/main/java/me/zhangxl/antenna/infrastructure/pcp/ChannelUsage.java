package me.zhangxl.antenna.infrastructure.pcp;

import me.zhangxl.antenna.infrastructure.base.ChannelManager;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.util.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存着信道的使用情况
 */
class ChannelUsage {
    /**
     * 一对正在通信的节点保存的信息
     */
    private static class CommunicateItem {
        final int srcId;
        final int targetId;
        final int channelId;

        CommunicateItem(int srcId, int targetId, int channelId) {
            this.srcId = srcId;
            this.targetId = targetId;
            this.channelId = channelId;
        }

        boolean hasId(int id) {
            return srcId == id || targetId == id;
        }
    }

    private final List<CommunicateItem> items = new ArrayList<>();

    /**
     * @param id 代表一个Station,是Station的编号
     * @return id所代表的Station目前是否处于空闲状态, 即正在Communicate的src
     * 以及target都没有id代表的station
     */
    boolean isIdFree(int id) {
        for (CommunicateItem item : items) {
            if (item.hasId(id)) {
                return false;
            }
        }
        return true;
    }

    int getNextFreeChannel() {
        for (int channel : ChannelManager.getInstance().getDataChannels()) {
            //简单当前的channel是不是free,如果是free就将这个channel返回
            boolean isTaken = false;
            for (CommunicateItem item : items) {
                if (item.channelId == channel) {
                    isTaken = true;
                    break;
                }
            }
            if (!isTaken) {
                return channel;
            }
        }
        throw new IllegalStateException("no data channel available");
    }

    int getItemSize() {
        return items.size();
    }

    void put(int srcId, int targetId, int channelId) {
        final CommunicateItem item = new CommunicateItem(srcId, targetId, channelId);
        items.add(item);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                items.remove(item);
            }
        }, Constant.getDataChannelDeadLine(), TimeTask.AFTER_RECEIVE);
    }
}
