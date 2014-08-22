package com.yeezhao.hound.ansj;

import love.cq.domain.Forest;
import love.cq.library.Library;
import love.cq.splitWord.GetWord;
import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

/**
 * Created by zhibin on 14-8-20.
 */
public class testAnsj {
   public static void main(String[] args){
       //基本分词方法
//       List<Term> parse = BaseAnalysis.parse("我今天在索答做了一些事情，呵呵呵呵，待会就去唱歌了哦。");
//       System.out.println(parse);

       //精确分词，无法辨别机构名称
//       List<Term> parse = ToAnalysis.parse("小胡于2020年毕业于斯坦福大学，获得博士后学位");
//       System.out.println(parse);

       //use crf model 但是提取大学机构名如下，效果不太好
//       List<Term> parse = NlpAnalysis.parse("孙鼎 北京大学化学与分子工程学院学士，清华大学经济管理学院硕士，人民大学公共管理学院博士，美国纽约大学文理学院和商学院双硕士");
//       System.out.println(parse);

//       // 使用自定义的词典   增加新词,中间按照'\t'隔开
//       UserDefineLibrary.insertWord("ansj中文分词", "userDefine", 1000);
//       List<Term> terms = ToAnalysis.parse("我觉得Ansj中文分词是一个不错的系统!我是王婆!");
//       System.out.println("增加新词例子:" + terms);
//       // 删除词语,只能删除.用户自定义的词典.
//       UserDefineLibrary.removeWord("ansj中文分词");
//       terms = ToAnalysis.parse("我觉得ansj中文分词是一个不错的系统!我是王婆!");
//       System.out.println("删除用户自定义词典例子:" + terms);

       String dic = "中国\t1\tzg\n人名\t2\n中国人民\t4\n人民\t3\n孙健\t5\nCSDN\t6\njava\t7\njava学习\t10\n";
       Forest forest = null;
       try {
           forest = Library.makeForest(new BufferedReader(new StringReader(dic)));
       } catch (Exception e) {
           e.printStackTrace();
       }

       /**
        * 删除一个单词
        */
       Library.removeWord(forest, "中国");
       /**
        * 增加一个新词
        */
       Library.insertWord(forest, "中国人");
       String content = "中国人名识别是中国人民的一个骄傲.孙健人民在CSDN中学到了很多最早iteye是java学习笔记叫javaeye但是java123只是一部分";
       GetWord udg = forest.getWord(content);

       String temp = null;
       while ((temp = udg.getFrontWords()) != null)
           System.out.println(temp + "\t\t" + udg.getParam(1) + "\t\t" + udg.getParam(2));


   }
}
