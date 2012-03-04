package com.oldratlee.cooma.ext6_inject.impl;

import java.util.List;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.ext6_inject.Ext6;

/**
 * @author oldratlee
 */
public class Ext6Impl2 implements Ext6 {
    List<String> list;

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public String echo(Configs config, String s) {
        throw new UnsupportedOperationException();
    }

}