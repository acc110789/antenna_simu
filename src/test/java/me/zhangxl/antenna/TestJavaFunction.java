package me.zhangxl.antenna;


import me.zhangxl.antenna.util.PrecisionUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 测试java一些函数的功能
 * Created by zhangxiaolong on 16/3/26.
 */
public class TestJavaFunction {

    @Test
    public void shortShift() {
        short cc = -1;
        System.out.println(Integer.toBinaryString(cc));
        cc >>>= 5;
        System.out.println(cc);
        System.out.println(Integer.toBinaryString(cc));
        System.out.println((short) 100000000);
        System.out.println(Charset.defaultCharset().displayName());
        System.out.println("it's 知乎日报".length());
        System.out.println("日".getBytes(Charset.forName("unicode")).length);
        System.out.println("it's 知乎日报".getBytes(Charset.forName("utf-8")).length);
        System.out.println("it's 知乎日报".getBytes(Charset.forName("gbk")).length);
        System.out.println("it's 知乎日报".getBytes(Charset.forName("ascii")).length);
    }

    @Test
    public void randomAccessFile() throws IOException {
        File file = File.createTempFile("zhangxl", "tmp");
        RandomAccessFile rs = null;
        try {
            rs = new RandomAccessFile(file, "rw");
            Random random = new Random(System.currentTimeMillis());
            for (int i = 0; i < 5; i++) {
                double value = random.nextDouble();
                rs.writeDouble(value);
            }
        } finally {
            IOUtils.closeQuietly(rs);
        }
        System.out.println("first display");
        display(file);
        try {
            rs = new RandomAccessFile(file, "rw");
            rs.seek(2 * 8);
            rs.writeDouble(12.333343333333);
        } finally {
            IOUtils.closeQuietly(rs);
        }
        System.out.println("second display");
        display(file);
    }

    private void display(File file) {
        RandomAccessFile rs = null;
        try {
            rs = new RandomAccessFile(file, "r");
            while (true) {
                try {
                    System.out.println(rs.readDouble());
                } catch (IOException e) {
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(rs);
        }
    }

    @Test
    public void concatNullString(){
        String aa = "1"+null;
        System.out.println(aa.length());
    }

    @Test
    public void pppLength(){
        Object[] objects = new Object[10];
        printLength(objects);
        printLength(objects,new Object(),new Object[20]);
    }

    private void printLength(Object... objects){
        System.out.println(objects.length);
    }

    @Test
    public void stringToNum(){
        String num = "2e-6";
        float deNum = Float.valueOf(num);
        System.out.format("%f",deNum);
    }

    @Test
    public void cloneFun() throws CloneNotSupportedException {
        A aa = new A();
        A aaclone = aa.clone();
        System.out.println(aa == aaclone);
        System.out.println(aa.b == aaclone.b);
    }

    class A implements Cloneable{

        public B b;

        public A(){
            b = new B();
        }

        @Override
        public A clone() throws CloneNotSupportedException {
            return (A)super.clone();
        }
    }

    class B implements  Cloneable{
        @Override
        public B clone() throws CloneNotSupportedException {
            return (B)super.clone();
        }
    }

    class C{

    }

    private <T> List<T> getType(){
        List<T> list = new ArrayList<T>();
        return list;
    }

    @Test
    public void mapExtendClass(){
        List<? super C> list = getType();
        list.add(new C());
    }

    @Test
    public void testDoubleCompare(){
        double a = 0;
        double b = PrecisionUtil.round(1/7);
        a += b;
        double c = PrecisionUtil.round(1/11);
        a += c;
        System.out.println(a==(b+c));
        System.out.println(0.05 + 0.01);
        System.out.println(PrecisionUtil.add(0.05,0.01));
        System.out.println(1.0 - 0.42);
        System.out.println(PrecisionUtil.sub(1.0,0.42));
        System.out.println(4.015 * 100);
        System.out.println(PrecisionUtil.mul(4.015,100.0));
        System.out.println(123.3 / 100);
        System.out.println(PrecisionUtil.div(123.3,100.0));

        System.out.println();
        System.out.println();
        System.out.println();
        BigDecimal big = new BigDecimal("12.22");
        BigDecimal big1 = new BigDecimal("12.23");
        big.add(big1);
        System.out.println("big :" + big);
    }

    @Test
    public void testTan(){
        double angle = PrecisionUtil.mul(PrecisionUtil.div(45,180),Math.PI);
        System.out.println("45 degree :" + PrecisionUtil.round(Math.tan(angle)));

        angle = PrecisionUtil.mul(PrecisionUtil.div(15,180),Math.PI);
        System.out.println("15 degree :" + PrecisionUtil.round(Math.tan(angle)));

        angle = PrecisionUtil.mul(PrecisionUtil.div(75,180),Math.PI);
        System.out.println("75 degree :" + PrecisionUtil.round(Math.tan(angle)));

        angle = PrecisionUtil.mul(PrecisionUtil.div(10,180),Math.PI);
        System.out.println("5 degree :" + PrecisionUtil.round(Math.tan(angle)));

        angle = PrecisionUtil.mul(PrecisionUtil.div(80,180),Math.PI);
        System.out.println("80 degree :" + PrecisionUtil.round(Math.tan(angle)));

        System.out.println("negative degree: " + PrecisionUtil.round(Math.atan(-1)));
    }

    @Test
    public void testListAdd(){
        Collection<Integer> list = new HashSet<>();
        list.add(1);
        list.add(1);
        assert  list.size() == 1;
    }



}
