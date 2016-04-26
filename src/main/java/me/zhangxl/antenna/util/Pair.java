package me.zhangxl.antenna.util;

/**
 * Created by zhangxiaolong on 16/4/7.
 */
public class Pair<A, B> {
    private final A fst;
    private final B snd;

    public Pair(A var1, B var2) {
        this.fst = var1;
        this.snd = var2;
    }

    public A getFirst(){
        return fst;
    }

    public B getSecond(){
        return snd;
    }
}
