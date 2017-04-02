package ru.mail.park;

/**
 * Created by victor on 01.04.17.
 */
public class CircleListIndex {
    private Integer index;
    private final Integer mod;

    public CircleListIndex(Integer mod){
        index = 0;
        this.mod = mod;
    }

    public void increment(){
        index = (index+1) % mod;
    }

    public Integer getIndex(){
        return index;
    }
}
