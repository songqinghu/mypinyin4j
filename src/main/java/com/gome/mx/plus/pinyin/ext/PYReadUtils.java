package com.gome.mx.plus.pinyin.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 输入汉字获取对应拼音的工具类
 * @author songqinghu
 *
 */
public class PYReadUtils {

    /**
     * 
     * @描述：输入汉字获取对应的全拼  可能是多音字  返回为数组类型 ---如果该汉字查不到则返回null
     * @param words
     * @return
     * @return String[]
     * @exception
     * @createTime：2016年4月6日
     * @author: songqinghu
     * @throws BadHanyuPinyinOutputFormatCombination 
     */
    public static String[] getFullPY(String words) throws BadHanyuPinyinOutputFormatCombination{
        
        StringBuffer buffer = new StringBuffer();
        
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        
        char[] chars = words.toCharArray();
        
        for (char c : chars) {
            if(c>128){//汉字
               String[] results  = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat);
                   
               for (int i = 0; i < results.length; i++) {
                buffer.append(results[i]);
                if(results.length - 1 != i){
                    buffer.append(",");
                 }
               }
               buffer.append(" ");
           }//不是汉字 --不处理 直接过滤掉
        }
        //所有汉字都变成了拼音  转换组合一下  将拼音拼凑起来
        return combination(buffer.toString());
    }
    //拼音封装去重复
    private static String[] combination(String all){
        ArrayList<Map<String, Integer>> list = new ArrayList<Map<String,Integer>>();
        
        String[] words = all.split(" ");//切为每个词
        for (String word : words) {
            String[] pys = word.split(",");//切出来每个词的每个拼音
            HashMap<String, Integer> map = new HashMap<String,Integer>();
            for (String py : pys) {//
                if(map.containsKey(py)){//去除重复拼音
                    Integer count = map.get(py);
                    map.put(py, count+1);
                }else{
                    map.put(py,1);
                }
            }
            list.add(map);//拼音顺序保持正确
        }
        //所有拼音处理完毕---进行拼凑
        return midMakeUp(list);
    }
    //组合拼音
    private static String[] midMakeUp(ArrayList<Map<String, Integer>> list){
        
        HashMap<String, Integer> firsts = null;
        
        for (Map<String, Integer> map : list) {
            
            HashMap<String, Integer> temp = new HashMap<String,Integer>();
            
            if(firsts !=null){//如果不是第一次--考虑组合问题
                for (String str : firsts.keySet()) {
                    for (String st : map.keySet()) {
                        temp.put(str + st, 1);//组合
                    }
                }
                
                if(temp != null && temp.size()>0){//清理容器  做容器转换
                    firsts.clear();
                }
                
            }else{//如果是第一次
                for (String str : map.keySet()) {
                    temp.put(str, 1);
                }
            }
            if(temp !=null && temp.size()>0){
                firsts = temp;
            }
        }
        //组合结束---调用方法转为string[] 
        
        return toStringArr(firsts);
    }
    
    private static String[] toStringArr(Map<String,Integer> map){
        if(map !=null && map.size()>0){
            String[] strs = new String[map.size()];
            Set<String> keySet = map.keySet();
            int i = 0;
            for (String key : keySet) {
                strs[i] = key;
                i++;
            }
            return strs;
        }
        return null;
    }
    
    
}
