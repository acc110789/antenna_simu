package me.zhangxl.antenna.infrastructure.pcp;

/**
 * 记录那些被nav的item
 * Created by zhangxiaolong on 16/7/15.
 */
class NavItem extends SaveItem {

    final int id;
    NavItem(int id,double endTimePoint) {
        super(endTimePoint);
        this.id = id;
    }

    @Override
    boolean hasId(int id) {
        return this.id == id;
    }

    @Override
    int getPeerSize() {
        return 1;
    }
}
