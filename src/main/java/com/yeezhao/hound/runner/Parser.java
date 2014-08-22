package com.yeezhao.hound.runner;

import com.yeezhao.commons.util.StringUtil;
import com.yeezhao.hound.http.HttpPageGetter;
import com.yeezhao.hound.util.FileResourceUtils;
import com.yeezhao.hound.vo.ContentVO;
import org.apache.hadoop.conf.Configuration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by zhibin on 14-8-18.
 */
public class Parser {
    String FILTERS = "传奇故事|代表性著作|个性|管理团队|链接|媒体报道|做最喜欢做的事";
    String DEGREES = "学士|硕士|博士|博士后";

    public ContentVO parseContent(String content) {
        ContentVO contentVO = new ContentVO();
        Document doc = Jsoup.parse(content);
        contentVO.setName(getName(doc));
        contentVO.setImgurl(getImgurl(doc));
        contentVO.setSpecialty(getSpecialty(doc));
        contentVO.setBio(getContentBio(doc));
        return contentVO;
    }

    //specialty
    private String getSpecialty(Document doc) {
        Element node = doc.getElementById("catenavi");
        String specialty = node.text();
        if (specialty.contains("    ")) {
            return specialty.split("    ")[0];
        } else {
            return specialty;
        }
    }

    //imgurl
    private String getImgurl(Document doc) {
        List<String> imgList = new ArrayList<String>();
        Element nameNode = doc.getElementById("doctitle");
        Elements imgs = doc.select("img");
        for (Element img : imgs) {
            String title = img.attr("title");
            if (title.equals(nameNode.text())) {
                if(isUrl(img.attr("src"))){
                    imgList.add(img.attr("src"));
                }else{
                    imgList.add("http://www.1000plan.org/wiki/" + img.attr("src"));
                }
            }
        }
        if (imgList.size() != 0) {
            return imgList.get(0);
        } else {
            return null;
        }
    }

    //name
    private String getName(Document doc) {
        List<String> imgList = new ArrayList<String>();
        Element nameNode = doc.getElementById("doctitle");
        return nameNode.text();
    }

    //bio
    private String getContentBio(Document doc) {
        String[] filterPhrase = FILTERS.split("\\|");
        List<String> titleList = new ArrayList<String>();
        Elements titles = doc.select("span");
        for (Element bioText : titles) {
            String test = bioText.attr("class");
            if (test.equals("texts")) {
                titleList.add(bioText.text());
            }
        }
        List<String> finalTitle = new ArrayList<String>();
        List<Integer> deleteIndex = new ArrayList<Integer>();
        for (int k = 0; k < titleList.size(); k++) {
            boolean checkFlag = false;
            for (String str : filterPhrase) {
                if (titleList.get(k).equals(str)) {
                    deleteIndex.add(k);
                    checkFlag = true;
                }
            }
            if (checkFlag == false) {
                finalTitle.add(titleList.get(k).trim());
            }
        }
        List<String> TextList = new ArrayList<String>();
        Elements divs = doc.select("div");
        boolean flag = false;
        for (Element div : divs) {
            String classValue = div.attr("class");
            if (classValue.equals("content_topp")) {
                if (flag == false) {
                    flag = true;
                } else {
                    String contentdiv = div.html();
                    contentdiv = Jsoup.clean(contentdiv, new Whitelist()
                            .addTags("br", "b"));
                    contentdiv = contentdiv.replaceAll("<br />", "");
                    contentdiv = contentdiv.replaceAll("&nbsp;", "");
                    contentdiv = contentdiv.replaceAll("<b>", "");
                    contentdiv = contentdiv.replaceAll("</b>", "");
                    TextList.add(contentdiv.trim());
                }
            }
        }
        List<String> bioList = new ArrayList<String>();
        for (int i = 0; i < TextList.size(); i++) {
            boolean Flag = false;
            for (int k : deleteIndex) {
                if (i == k) {
                    Flag = true;
                }
            }
            if (Flag == false) {
                bioList.add(TextList.get(i));
            }
        }
        StringBuffer bio = new StringBuffer();
        for (int i = 0; i < finalTitle.size(); i++) {
            bio.append((i + 1) + ": " + finalTitle.get(i)).append("\n").append("\r\n");
            bio.append(bioList.get(i)).append("\r\n").append("\r\n");
        }
        return bio.toString();
    }


    public static void main(String[] args) {
        Parser parser = new Parser();
        String content = HttpPageGetter.get("http://www.1000plan.org/wiki/index.php?doc-view-345", HttpPageGetter.PROXY_TYPE.NONE);
        ContentVO cont = parser.parseContent(content);
        System.out.println("name :" + cont.getName());
        System.out.println("specialty :" + cont.getSpecialty());
        System.out.println("imgurl :" + cont.getImgurl());
        System.out.println(cont.getBio());
        parser.getSchoolDegree(cont.getBio());
        if ((parser.getSchoolDegree(cont.getBio())) != null) {
            cont.setDegreeAndSchool(parser.getSchoolDegree(cont.getBio()));
        }
        System.out.println("degree 长度： " + cont.getDegreeAndSchool().size());
        for(String school : cont.getDegreeAndSchool()){
            System.out.println(school);
        }
    }

    public List<String> getSchoolDegree(String bio) {
        String[] degrees = DEGREES.split("\\|");
        List<String> degreeSchoole = new ArrayList<String>();
        for (String degree : degrees) {
            if (bio.contains(degree)) {
                boolean flag = true;
                //System.out.println("包含了： " + degree);
                int indexDegree = bio.indexOf(degree, 0);
                if (indexDegree == -1) {
                    flag = false;
                }
                while (flag == true) {
                    String school = extractSchool(bio, indexDegree);
                    if (school != null) {
                        degreeSchoole.add(degree + "|" + school);
                    }
                    indexDegree = bio.indexOf(degree, indexDegree + 4);
                    if (indexDegree == -1) {
                        flag = false;
                    }
                }
            }
        }
        return degreeSchoole;
    }

    public String extractSchool(String bio, int lastIndex) {
        Configuration conf = new Configuration();
        conf.addResource("school-config.xml");
        try {
            List<String> schools = FileResourceUtils.getResourcesAsStrings(conf, "ext.resource.school");
            String content;
            if (lastIndex <= 30) {
                content = bio.substring(0, lastIndex);
            } else {
                content = bio.substring(lastIndex - 30, lastIndex);
            }

            for (String school : schools) {
                if (content.contains(school)) {
                    return school;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //1000planNAME
    public String generateUUIDWith16Bit(String str) {
        return StringUtil.genMD5Val(str).substring(8, 24);
    }

    public String specialtyMap(String specialty) {
        if (specialty.equals("生物医药与生物技术")) return "Biology";
        if (specialty.equals("能源、资源与环境")) return "Environmental Sciences";
        if (specialty.equals("经济、金融与管理")) return "Economics & Business";
        if (specialty.equals("信息科学与技术")) return "Computer Science";
        if (specialty.equals("高新技术产业")) return "Engineering";
        if (specialty.equals("工程与材料")) return "Material Science";
        if (specialty.equals("化学化工")) return "Chemistry";
        if (specialty.equals("数学物理")) return "Mathematics";
        return null;
    }

    public String chooseDegreeSchool(List<String> schools) {
        if (schools.size() == 0) return null;
        String[] degrees = DEGREES.split("\\|");
        String bdegree = null;
        String mdegree = null;
        String ddegree = null;
        String odegree = null;
        for (String degreeSchool : schools) {
            if (degreeSchool.contains(degrees[0])) {
                if (bdegree == null) {
                    bdegree = degreeSchool;
                }
            }
            if (degreeSchool.contains(degrees[1])) {
                if (mdegree == null) {
                    mdegree = degreeSchool;
                }
            }
            if (degreeSchool.contains(degrees[2])) {
                if (ddegree == null) {
                    ddegree = degreeSchool;
                }
            }
            if (degreeSchool.contains(degrees[3])) {
                if (odegree == null) {
                    odegree = degreeSchool;
                }
            }
        }
        if (odegree != null) {
            return odegree;
        }
        if (ddegree != null) {
            return ddegree;
        }
        if (mdegree != null) {
            return mdegree;
        }
        if (bdegree != null) {
            return bdegree;
        }
        return null;
    }


    public static boolean isUrl(String url){
        Pattern p = Pattern.compile("^http[s]?:\\/\\/([\\w-]+\\.)+[\\w-]+([\\w-./?%&=]*)?$");
        return p.matcher(url).find();
    }

}
