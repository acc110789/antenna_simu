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

    @Override
    public boolean equals(Object obj) {
        //检查类型
        if(!(obj instanceof Pair)){
            return false;
        }
        Pair target = (Pair) obj;

        //检查第一个参数
        boolean firstCorrect = fst == null ? target.getFirst() == null : fst.equals(target.getFirst());
        if(!firstCorrect){
            return false;
        }

        //检查第二个参数
        return snd == null ? target.getSecond() == null : snd.equals(target.getSecond());
    }
}
