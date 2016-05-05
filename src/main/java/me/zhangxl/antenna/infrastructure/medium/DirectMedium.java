package me.zhangxl.antenna.infrastructure.medium;

import me.zhangxl.antenna.frame.Frame;
import me.zhangxl.antenna.infrastructure.Station;
import me.zhangxl.antenna.util.Config;
import me.zhangxl.antenna.util.Pair;
import me.zhangxl.antenna.util.PrecisionUtil;

import java.util.*;

/**
 * Created by zhangxiaolong on 16/4/10.
 */
public class DirectMedium extends Medium {
    private static final Map<Station, Info> sMap = new HashMap<>();

    @Override
    List<Station> getStationToReceive(Station source, Frame frame) {
        //先计算出frame具体在source的哪一个扇区,然后将那一个扇区所有的lists全部返回

        //找到target Station
        int targetId = frame.getTargetId();
        Station target = null;
        for(Station station : stationList){
            if(station.getId() == targetId){
                target = station;
            }
        }
        //找到target Station的坐标
        double angle = getAngle(target.getAxis(), source.getAxis());
        double unit = PrecisionUtil.div(360, Config.getInstance().getPart());//每隔unit设置一个跨度
        double d_index = PrecisionUtil.div(angle, unit);
        int index = ((int) Math.floor(d_index));
        index += frame.getSectorOffset();
        index %= Config.getInstance().getPart();
        return sMap.get(source).getStations(index);
    }

    public static Map<Station, Info> getMap() {
        return sMap;
    }

    void analysisStationLocation() {
        for (Station station : stationList) {
            sMap.put(station, new Info());
        }
        for (Station station : stationList) {
            //分析每一个station的位置信息
            List<Station> neighbors = new ArrayList<>(stationList);
            neighbors.remove(station);
            for (Station neighbor : neighbors) {
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
    private double getAngle(Pair<Double, Double> target, Pair<Double, Double> origin) {
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
        private ArrayList[] data = new ArrayList[Config.getInstance().getPart()];

        {
            for (int i = 0; i < data.length; i++) {
                if(data[i] == null){
                    data[i] = new ArrayList();
                }
            }
        }

        void add(int index, Station station) {
            data[index].add(station);
        }

        public List<Station> getStations(int index) {
            return new ArrayList<Station>(data[index]);
        }
    }
}
