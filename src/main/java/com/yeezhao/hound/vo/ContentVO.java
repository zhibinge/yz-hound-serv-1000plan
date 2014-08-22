package com.yeezhao.hound.vo;

import java.util.List;
import java.util.Map;

/**
 * Created by zhibin on 14-8-19.
 */
public class ContentVO {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    private String name;
    private String imgurl;
    private String specialty;
    private String bio;

    public List<String> getDegreeAndSchool() {
        return degreeAndSchool;
    }

    public void setDegreeAndSchool(List<String> degreeAndSchool) {
        this.degreeAndSchool = degreeAndSchool;
    }

    private List<String> degreeAndSchool;
}
