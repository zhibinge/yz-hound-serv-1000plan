package com.yeezhao.hound.ansj;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;

import java.util.Collection;

/**
 * Created by zhibin on 14-8-21.
 */
public class KeyWordCompuerDemo {
    public static void main(String[] args) {
        KeyWordComputer kwc = new KeyWordComputer(5);
        String title = "胡志斌毕业于沃顿商学院";
        String content = "最新消息，出国2年的胡志斌同学，今天的沃顿商学院毕业典礼上，作为优秀毕业生进行了毕业演讲，同行的有著名沃顿校友郎咸平一同参加！";
        Collection<Keyword> result = kwc.computeArticleTfidf(title, content);
        System.out.println(result);
    }
}
