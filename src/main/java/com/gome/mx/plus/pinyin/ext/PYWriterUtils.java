package com.gome.mx.plus.pinyin.ext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;

import net.sourceforge.pinyin4j.ChineseToPinyinResource;
import net.sourceforge.pinyin4j.ResourceHelper;
import net.sourceforge.pinyin4j.multipinyin.MultiPinyinConfig;
import net.sourceforge.pinyin4j.multipinyin.Trie;
/**
 * 将汉语和拼音写入指定的文件中--文件位置可以指定
 * 并且能够动态的加载  不需要重启服务
 * 还能指定是否重新写 还是追加的方式
 * 还能够将原来已经存在的拼音合并过来--可以指定
 * @author songqinghu
 *
 */
public class PYWriterUtils {

    //这里改为系统的绝对路径
    private static String path;
    
    private static boolean flag = true;//可以设置文件位置
    /**
     * @描述：获取配置文件的位置 ---只能设置一次
     * @return void
     * @exception
     * @createTime：2016年4月6日
     * @author: songqinghu
     */
    public static void setPath(String path){
        if(flag){
            PYWriterUtils.path = path;
            flag = false;//只能设置 一次
        }
    }
    
    public static String getPath(){
        return PYWriterUtils.path;
    }
    
    private static Class pathClass = PYWriterUtils.class;
    

    /**
     * 
     * @描述：默认写入的方式  设置为追加模式  合并已经存在的拼音为一个
     * @param word  汉字
     * @param pinyin 拼音
     * @param voice  声调
     * @return
     * @return boolean  是否成功
     * @exception
     * @createTime：2016年4月6日
     * @author: songqinghu
     * @throws Exception 
     */
    public static boolean dufaultWriter(String word,String pinyin,Integer voice) throws Exception{
        return writerControler(word, pinyin, voice, true, true);
    }
    /**
     * 
     * @描述：可以设置的写入方式  --这里还要增加一个批量写入的功能  本方法只是处理一个汉字
     * @param word  汉字
     * @param pinyin 拼音
     * @param voice  声调
     * @param additional 是否追加到文件后
     * @param merge 是否合并已经出现的拼音到文件中
     * @return
     * @return boolean
     * @exception
     * @createTime：2016年4月6日
     * @author: songqinghu
     * @throws Exception 
     * 龦
     */
    public static boolean writerControler(String word,String pinyin,Integer voice,
            boolean additional ,boolean merge) throws Exception{
        
        String path = PYWriterUtils.path;
        if (path != null) {
            File userMultiPinyinFile = new File(path);
            if (userMultiPinyinFile.exists()) {
                //获取
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(userMultiPinyinFile, additional)));
                //添加音调
                pinyin = pinyin + voice;
                //写入--16进制  查询 --
                if(word !=null && word.length()>0){
                    char c = word.toCharArray()[0];
                    if(c>128){//是汉字
                        String unicode = Integer.toHexString(c).toUpperCase();//编码
                        if(merge){//如果要合并
                            Trie trie = ChineseToPinyinResource.getInstance().getUnicodeToHanyuPinyinTable();
                            
                            if(trie.get(unicode)!=null){ //存在了编码和拼音对应关系---这里最好在判断一次是否存在了该拼音
                                String before = trie.get(unicode).getPinyin();
                                before = before.trim().substring(1, before.trim().length()-1);//去除()
                                //存在了 就不添加进去了
                                boolean flag = false;
                                String[] words = before.split(",");
                                for (String str : words) {
                                    if(str.equals(pinyin)){
                                        flag = true; //存在该拼音
                                        break;
                                    }
                                }
                               if(flag){
                                   pinyin = before;
                               }else{
                                   pinyin = before +Field.COMMA+ pinyin ;
                               }
                            }
                            //不存在  不需要改变pinyin
                        }
                        pinyin = addSymbol(pinyin);
                        writer.write(unicode+Field.SPACE+pinyin);
                        writer.newLine();
                    }
                }
                writer.flush();
                writer.close();
                //写入完成  更新词库
                reloadText();
                return true;
            }
        }else{
            throw new Exception("找不到用户扩展字典");
        }
       return false;
    }
    
    /**
     * 完成批量添加的功能
     */
    /**
     * 
     * @描述：批量添加汉字和拼音的映射关系到自定义词库中----这里有个问题 当 批量输入一个多音字 拼音都是map中同一个key时只能提交成功一个--建议提交两次
     * @param contents  汉字  拼音  音调  这里一个汉字  可以输入多个拼音了
     * @param additional 是否追加到文件后
     * @param merge 是否合并已经出现的拼音到文件中
     * @return
     * @return boolean
     * @exception
     * @createTime：2016年4月7日
     * @author: songqinghu
     */
    public static boolean writerBatch(Map<String,Map<String,Integer>> contents,boolean additional ,boolean merge){
        //加载文件部分
        BufferedWriter writer =null;
        try {
            if (path != null) {
                File userMultiPinyinFile = new File(path);
                if (userMultiPinyinFile.exists()) {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(userMultiPinyinFile, additional)));
            //写入处理部分
            Set<Entry<String, Map<String, Integer>>> entrySet = contents.entrySet();
            for (Entry<String, Map<String, Integer>> entry : entrySet) {
                String word = entry.getKey().trim();//汉语
                String pinyin = "";
                for (Entry<String, Integer> content : entry.getValue().entrySet()) {
                    String py = content.getKey().trim();
                    Integer voice = content.getValue();
                    pinyin = pinyin + py + voice+",";
                }
                //拼音添加结束  去除最后一个,
                pinyin = pinyin.substring(0, pinyin.length()-1);
                //汉字和拼音都已经处理完毕 进入单个词语写入模块 --方法 抽取出来公用
                String line = midWriter(word, pinyin, merge);
                if(line != null){
                    writer.write(line);
                    writer.newLine();
                }
            }
            writer.flush();
            return true;
                }
           }else{
               throw new  Exception("请配置用户词典绝对路径");
           }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(writer!=null)
                   writer.close();
                PYWriterUtils.reloadText();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 
     * @描述：方法抽取--对单个字进行处理
     * @return
     * @return String 组合后的一行要写入的   形式    E4A3 (ang3,yi1,wang3)
     * @exception
     * @createTime：2016年4月7日
     * @author: songqinghu
     */
    private static String midWriter(String word ,String pinyin,boolean merge){
        
        if(word !=null && word.length()>0){
            char c = word.toCharArray()[0];
            if(c>128){//如果是汉字
               String unicode  = Integer.toHexString(c).toUpperCase();//变为16进制
               if(merge){//如果要合并 需要先取出来  在合并  取不到还要处理一下
                   //获取到总的资源池
                   Trie trie = ChineseToPinyinResource.getInstance().getUnicodeToHanyuPinyinTable();
                   //如果存在该词语的拼音
                   if(trie.get(unicode)!=null &&trie.get(unicode).getPinyin()!=null){
                       String before = trie.get(unicode).getPinyin();
                       //对已经处在字符串进行处理 --(xxx) (xxxx,xxxx) 
                       before = before.trim().substring(1, before.trim().length()-1);//去除()
                       //如果存在了  就不再重复添加了
                       String[] splits = before.split(",");
                       String[] strings = pinyin.trim().split(",");
                       Set<String> temp  = new HashSet<String>();
                       //去重复
                       for (String split : splits) {
                           temp.add(split.trim());
                       }
                       for (String string : strings) {
                          temp.add(string);
                       }
                       pinyin ="";
                       for (String tem : temp) {
                         pinyin = pinyin + tem+Field.COMMA;
                       }
                       pinyin =  pinyin.substring(0,pinyin.length()-1);//去除最后一个,
                   }
                   //不存在 直接 保持拼音不变
               }
               //组合成写入的格式
               pinyin = addSymbol(pinyin);
               
               return unicode + Field.SPACE+pinyin;
            }
        }
        return null;
    }
    
    /**
     * 
     * @描述：默认批量写入功能
     * @param contents
     * @return
     * @return boolean
     * @exception
     * @createTime：2016年4月7日
     * @author: songqinghu
     */
    public static boolean defaultWriterBatch(Map<String,Map<String,Integer>> contents){
        
        return writerBatch(contents, true, true);
    }
    
    /**
     * 
     * @描述：当自定义文件需要更新时,调用方法 重新加载自己的配置文件
     * @return
     * @return boolean
     * @exception
     * @createTime：2016年4月6日
     * @author: songqinghu
     * @throws IOException 
     */
    public static boolean reloadText() throws IOException{
        
        if (path != null) {
            File userMultiPinyinFile = new File(path);
            FileInputStream is = new FileInputStream(userMultiPinyinFile);
            if(is !=null){
              ChineseToPinyinResource.getInstance().getUnicodeToHanyuPinyinTable().load(is);
              return true;
            }
        }
        return false;
    }
    
    
    
    /**
     * 添加操作符号
     */
    private static String addSymbol(String pinyin){
        return Field.LEFT_BRACKET+pinyin+Field.RIGHT_BRACKET;
    }
    
    class Field {
        static final String LEFT_BRACKET = "(";

        static final String RIGHT_BRACKET = ")";

        static final String COMMA = ",";
        
        static final String SPACE = " ";
    }
}
