
package com.gome.mx.plus.pinyin.ext;

public enum Voice {

    One(1),Two(2),Three(3),Four(4);
    
    private  final Integer value;
    
    Voice(Integer value){
        this.value = value;
    }
    
    public Integer getValue(){
        return value;
    }
}
