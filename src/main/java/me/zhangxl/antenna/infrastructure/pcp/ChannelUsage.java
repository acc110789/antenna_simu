package me.zhangxl.antenna.infrastructure.pcp;

import me.zhangxl.antenna.infrastructure.base.ChannelManager;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.clock.TimeTask;
import me.zhangxl.antenna.util.Constant;
import me.zhangxl.antenna.util.PrecisionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存着信道的使用情况
 */
class ChannelUsage {

    private final List<SaveItem> items = new ArrayList<>();

    /**
     * @param id 代表一个Station,是Station的编号
     * @return id所代表的Station目前是否处于空闲状态, 即正在Communicate的src
     * 以及target都没有id代表的station
     */
    boolean isIdFree(int id) {
        for (SaveItem item : items) {
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
            for (SaveItem item : items) {
                if (item instanceof NavItem) {
                    continue;
                }
                CommunicatePairItem pairItem = (CommunicatePairItem) item;
                if (pairItem.channelId == channel) {
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
        int result = 0;
        for(SaveItem item : items){
            result += item.getPeerSize();
        }
        return result;
    }

    void putCommunicatePair(int srcId, int targetId, int channelId) {
        final CommunicatePairItem item = new CommunicatePairItem(srcId, targetId, channelId,
                PrecisionUtil.add(TimeController.getInstance().getCurrentTime(), Constant.getDataChannelDeadLine()));
        items.add(item);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                items.remove(item);
            }
        }, Constant.getDataChannelDeadLine(), TimeTask.AFTER_RECEIVE);
    }

    void putNavItem(int id, double duration) {
        final NavItem item = new NavItem(id, PrecisionUtil.add(TimeController.getInstance().getCurrentTime(), duration));
        items.add(item);
        TimeController.getInstance().post(new Runnable() {
            @Override
            public void run() {
                items.remove(item);
            }
        }, duration);
    }

    /**
     * @return 等待一个最快被释放的dataChannel所需要的时间
     */
    double getShortestWaitTime() {
        double time = Double.MAX_VALUE;
        for (SaveItem item : items) {
            if (item instanceof NavItem) {
                continue;
            }
            if (PrecisionUtil.largeThan(time, item.getWaitingTime())) {
                time = item.getWaitingTime();
            }
        }
        if (time == Double.MAX_VALUE) {
            throw new IllegalStateException("wrong");
        }
        return time;
    }

    /**
     * @param targetId
     * @return 返回targetId代表的Station空闲还需要等待的时间
     */
    double getWaitingTimeNeeded(int targetId) {
        SaveItem target = null;
        for (SaveItem item : items) {
            if (item.hasId(targetId)) {
                target = item;
            }
        }
        if (target == null) {
            throw new IllegalStateException("targetId not found");
        }
        return target.getWaitingTime();
    }

    /**
     * @param targetId station的id
     * @return 目标station是否已经成为某对连接的接受者了
     */
    boolean isReceiver(int targetId) {
        CommunicatePairItem pairItem;
        for (SaveItem item : items) {
            if (item instanceof CommunicatePairItem) {
                pairItem = (CommunicatePairItem) item;
                if (pairItem.targetId == targetId) {
                    return true;
                }
            }
        }
        return false;
    }
}
