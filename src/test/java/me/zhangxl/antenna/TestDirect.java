package me.zhangxl.antenna;

import me.zhangxl.antenna.infrastructure.Locatable;
import me.zhangxl.antenna.infrastructure.station.Station;
import me.zhangxl.antenna.infrastructure.clock.TimeController;
import me.zhangxl.antenna.infrastructure.medium.DirectMedium;
import me.zhangxl.antenna.infrastructure.medium.Medium;
import me.zhangxl.antenna.util.Config;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangxiaolong on 16/4/25.
 */
public class TestDirect {

    @Test
    public void testDirectAnalyze() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Config.getInstance();

        Class<Config> configClass = Config.class;
        Field field1 = configClass.getDeclaredField("part");
        field1.setAccessible(true);
        field1.setInt(Config.getInstance(), 8);

        Field field = configClass.getDeclaredField("antennaMode");
        field.setAccessible(true);
        field.setInt(Config.getInstance(), 1);//定向天线模式

        new Station(1,1,0);
        new Station(2,0,1);
        new Station(3,-1,0);

        Medium.getInstance();
        TimeController.getInstance();
        Method method = TimeController.class.getDeclaredMethod("preLoop");
        method.setAccessible(true);
        method.invoke(TimeController.getInstance());

        Map<Locatable, DirectMedium.Info> map = DirectMedium.getMap();
        for (Map.Entry<Locatable, DirectMedium.Info> entry : map.entrySet()) {
            System.out.println(entry.getKey().getId() + ":");
            for (int i = 0; i < Config.getInstance().getPart(); i++) {
                List<Locatable> stations = entry.getValue().getStations(i);
                System.out.println("    第" + i + "个扇区:");
                for(Locatable station : stations){
                    System.out.println("            contain "+station.getId());
                }
            }
        }
    }

}
