package com.oldratlee.cooma.ext2;

import com.oldratlee.cooma.Configs;


/**
 * @author oldratlee
 *
 */
public class UrlHolder {
    private Double Num;
    
    private Configs config;
    
    private String name;
    
    private int age;
    
    public Double getNum() {
        return Num;
    }

    public void setNum(Double num) {
        Num = num;
    }

    public Configs getUrl() {
        return config;
    }

    public void setUrl(Configs config) {
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}