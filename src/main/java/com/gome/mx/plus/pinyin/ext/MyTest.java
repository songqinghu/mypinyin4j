package com.gome.mx.plus.pinyin.ext;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class MyTest {

    
    public static void main(String[] args) throws BadHanyuPinyinOutputFormatCombination {
        //PYWriterUtils.setPath("D:\\wordconfig\\gome_pinyin_ext.txt");
        testMap();
        testOne();
    }
    /**
     * @描述：测试map添加的方式
     * @return void
     * @exception
     * @createTime：2016年4月7日
     * @author: songqinghu
     */
    public static void testMap(){
        PYWriterUtils.setPath("D:\\wordconfig\\gome_pinyin_ext.txt");
        String word = "龦";
        //查询 测试
        try {
            String[] fullPY = PYReadUtils.getFullPY(word);
            if(fullPY == null){
                System.out.println("为查询到");
            }else{
                for (String py : fullPY) {
                    System.out.println(py);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        //3007 (ling2)
        Map<String, Map<String, Integer>> contents  = new HashMap<String,Map<String, Integer>>();
        
        Map<String, Integer> content= new HashMap<String,Integer>();
       
        content.put("ling", Voice.One.getValue());
        content.put("ling", Voice.Two.getValue());
        content.put("test", Voice.Two.getValue());
        contents.put(word, content);
        PYWriterUtils.writerBatch(contents  , true, true);
        
        //查询 测试
        try {
            String[] fullPY = PYReadUtils.getFullPY(word);
            if(fullPY == null){
                System.out.println("为查询到");
            }else{
                for (String py : fullPY) {
                    System.out.println(py);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        
        
    }
    
    public static void testOne(){
//      String[] fullPY = PYReadUtils.getFullPY("龦");
//      for (String string : fullPY) {
//          System.out.println(string);
//      }
//      String word = "骉";
      String word = "龦";
      try {
          //写入全新的字符到文件中
          PYWriterUtils.writerControler(word, "test", Voice.Two.getValue(),true, true);
          String[] fullPY = PYReadUtils.getFullPY(word);
          if(fullPY == null){
              System.out.println("没有查到");
          }else{
              System.out.println("查到");
              for (String string : fullPY) {
                  System.out.println(string);
              }
          }
          //PYWriterUtils.reloadText();
          String[] full = PYReadUtils.getFullPY(word);
          if(full == null){
              System.out.println("没有查到");
          }else{
              System.out.println("查到");
              for (String string : full) {
                  System.out.println(string);
              }
          }
          
          
      } catch (Exception e) {
          e.printStackTrace();
      }
    }
}
