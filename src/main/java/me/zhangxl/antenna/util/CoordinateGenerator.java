package me.zhangxl.antenna.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhangxiaolong on 16/5/26.
 */
public class CoordinateGenerator implements Generator<Pair<Double,Double>> {

    private static CoordinateGenerator sInstance = new CoordinateGenerator();

    public static Generator<Pair<Double,Double>> getInstance(){
        return sInstance;
    }

    private final List<Pair<Double,Double>> existCoordinates = new ArrayList<>();
    {
        //坐标(0,0) 是PcpStation的坐标
        existCoordinates.add(new Pair<>((double)0,(double)0));
    }
    private final Random random = new Random(System.nanoTime());
    private final int xScale = 11;
    private final int yScale = 11;

    @Override
    public Pair<Double, Double> next() {
        while (true){
            Pair<Double,Double> value = nextInner();
            if(!existCoordinates.contains(value)){
                existCoordinates.add(value);
                return value;
            }
        }
    }

    private Pair<Double, Double> nextInner(){
        int x = random.nextInt(xScale)-5;// x的取值范围:[-5,5]
        int y = random.nextInt(yScale)-5;// y的取值范围:[-5,5]
        return new Pair<>((double) x, (double) y);
    }
}
