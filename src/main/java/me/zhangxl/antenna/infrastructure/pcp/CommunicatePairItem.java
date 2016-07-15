package me.zhangxl.antenna.infrastructure.pcp;

/**
 * 一对正在通信的节点保存的信息
 * Created by zhangxiaolong on 16/7/15.
 */
class CommunicatePairItem extends SaveItem {
    final int srcId;
    final int targetId;
    final int channelId;

    CommunicatePairItem(int srcId, int targetId, int channelId, double endTimePoint) {
        super(endTimePoint);
        this.srcId = srcId;
        this.targetId = targetId;
        this.channelId = channelId;
    }

    @Override
    boolean hasId(int id) {
        return srcId == id || targetId == id;
    }

    @Override
    int getPeerSize() {
        return 2;
    }
}
