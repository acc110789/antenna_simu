package me.zhangxl.antenna.frame_process;

import me.zhangxl.antenna.frame.*;
import me.zhangxl.antenna.infrastructure.Station;

/**
 * Created by zhangxiaolong on 16/6/1.
 */
public class ProcessorHelper {

    public static void process(Station station, Frame frame){
        Processor processor;
        if(frame instanceof RtsFrame){
            processor = new RtsProcessor(station);
        } else if(frame instanceof CtsFrame){
            processor = new CtsProcessor(station);
        } else if(frame instanceof DataFrame){
            processor = new DataProcessor(station);
        } else if(frame instanceof AckFrame){
            processor = new AckProcessor(station);
        } else {
            throw new IllegalStateException();
        }
        processor.process(frame);
    }
}
