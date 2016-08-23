package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Locatable;
import me.zhangxl.antenna.infrastructure.host_peer.PcpStation;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Pair;
import me.zhangxl.antenna.util.PrecisionUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangxiaolong on 16/4/10.
 */
public class DirectMedium extends Medium {
    /**
     * 保存着每一个节点的扇区信息
     */
    static final Map<Locatable, Info> sMap = new HashMap<>();

    /**
     * 查看target在source的哪一个扇区
     * @param source
     * @param targetId
     * @return
     */
    public static int getSectorIndex(Locatable source,int targetId){
        Locatable target = null;
        for(Locatable station : stationList){
            if(station.getId() == targetId){
                target = station;
                break;
            }
        }
        assert target != null;
        return sMap.get(source).getIndex(target);
    }

    public static Info getPcpInfo(){
        Locatable target = null;
        for(Locatable locatable : sMap.keySet()){
            if (locatable.getId() == PcpStation.getInstance().getId()) {
                target = locatable;
            }
        }
        assert target != null;
        return sMap.get(target);
    }

    /**
     * @param aId  a节点的id
     * @param bId   b节点的id
     * @param cId   c节点的id
     * @return  c节点是否经过a和b之间的通信,
     */
    public static boolean cPass(int aId,int bId,int cId){
        //先找到三个id对应的节点
        Locatable a = null,b = null,c = null;
        for(Locatable locatable : stationList){
            if(locatable.getId() == aId){
                a = locatable;
            } else if(locatable.getId() == bId){
                b = locatable;
            } else if(locatable.getId() == cId){
                c = locatable;
            }
        }
        assert (a != null);
        assert (b != null);
        assert (c != null);

        //先以a为基准点,查看b和c是否在a的同一个扇区
        //具体算法是先找到b所在的扇区,然后看扇区里面是不是有c
       Info infoa = sMap.get(a);
        if(infoa.getIndex(b) == infoa.getIndex(c)){
            return true;
        }
        Info infob = sMap.get(b);
        return infob.getIndex(a) == infob.getIndex(c);
    }

    /**
     * 原来是全向接受,只需要过滤一次,找到source扇区里面的所有节点即可
     * 但是现在改成了定向接受,需要过滤两次,第一次过滤先找到source扇区里面的所有的节点
     * 从第一次过滤的节点找到那些source节点暴露的节点
     * @param source
     * @param frame
     * @param sector
     * @return
     */
    List<Locatable> getStationToReceive(Locatable source ,Frame frame,int sector){
        //第一步,先找到source的sector扇区里面的所有Locatable
        return new TargetFilters(source,
                (sector < 0) ? getStationToReceive(source,frame) : new SourceFilterBySector(source,sector).filt()
        ).filt();
    }

    //先计算出frame具体在source的哪一个扇区,然后将那一个扇区所有的lists全部返回
    @Override
    List<Locatable> getStationToReceive(Locatable source, Frame frame) {
        //根据frame中提供的station的id找到具体的station(target)
        return new SourceFilterByFrame(source,frame).filt();

    }

    public static Map<Locatable, Info> getMap() {
        return sMap;
    }

    void analysisStationLocation() {
        for (Locatable station : stationList) {
            sMap.put(station, new Info());
        }
        for (Locatable station : stationList) {
            //分析每一个station的位置信息
            List<Locatable> neighbors = new ArrayList<>(stationList);
            neighbors.remove(station);
            for (Locatable neighbor : neighbors) {
                double angle = getAngle(neighbor.getAxis(), station.getAxis());
                //每隔unit设置一个跨度
                double unit = PrecisionUtil.div(360, Config.getInstance().getPart());
                double d_index = PrecisionUtil.div(angle, unit);
                int index = ((int) Math.floor(d_index));
                sMap.get(station).add(index, neighbor);
            }
        }
    }

    /**
     * @param target 目标点
     * @param origin 参照点
     * @return 返回目标点相对于参照点的夹角
     */
    static double getAngle(Pair<Double, Double> target, Pair<Double, Double> origin) {
        double relativeX = PrecisionUtil.sub(target.getFirst(), origin.getFirst());
        double relativeY = PrecisionUtil.sub(target.getSecond(), origin.getSecond());
        //保证两个节点的位置是不同的
        if (relativeX == 0 && relativeY == 0) {
            throw new RuntimeException("position is repeated");
        }
        if (relativeX == 0) {
            if (relativeY > 0) {
                return 90;
            } else {
                return 270;
            }
        } else if (relativeY == 0) {
            if (relativeX > 0) {
                return 0;
            } else {
                return 180;
            }
        } else {
            // TODO: 16/4/25 y/x的值不要太大 ,如果这个比值太大,要换一下算法
            double tan_value = PrecisionUtil.div(relativeY, relativeX);
            double angle = Math.atan(tan_value);
            double factor = PrecisionUtil.div(180, Math.PI);
            angle = PrecisionUtil.mul(angle, factor);
            if (angle > 0) {
                if (relativeX > 0 && relativeY > 0) {
                    //第一象限
                    return angle;
                } else if (relativeX < 0 && relativeY < 0) {
                    //第三象限
                    return PrecisionUtil.add(angle, 180);
                } else {
                    throw new RuntimeException("impossible 1");
                }
            } else if (angle < 0) {
                angle = -angle;
                if (relativeX < 0 && relativeY > 0) {
                    //第二象限
                    return PrecisionUtil.sub(180, angle);
                } else if (relativeX > 0 && relativeY < 0) {
                    //第四象限
                    return PrecisionUtil.sub(360, angle);
                } else {
                    throw new RuntimeException("impossible 2");
                }
            } else {
                throw new RuntimeException("impossible 3");
            }
        }
        //要注意误差
    }

    //区域的编号从0开始算
    public class Info {
        private ArrayList<Locatable>[] data = (ArrayList<Locatable>[]) Array.newInstance(
                ArrayList.class,Config.getInstance().getPart());

        {
            for (int i = 0; i < data.length; i++) {
                if(data[i] == null){
                    data[i] = new ArrayList();
                }
            }
        }

        void add(int index, Locatable station) {
            data[index].add(station);
        }

        public List<Locatable> getStations(int index) {
            return new ArrayList<>(data[index]);
        }

        int getIndex(Locatable locatable){
            for(int index = 0;index < Config.getInstance().getPart();index ++){
                for(Locatable locatable1 : getStations(index)){
                    if(locatable1.getId() == locatable.getId()){
                        return index;
                    }
                }
            }
            throw new IllegalStateException("not found");
        }

        public int getIndex(int id){
            //先找到id对应的节点
            Locatable target = null;
            for(Locatable locatable : stationList){
                if(locatable.getId() == id){
                    target = locatable;
                }
            }
            assert target != null;
            return getIndex(target);
        }
    }
}
