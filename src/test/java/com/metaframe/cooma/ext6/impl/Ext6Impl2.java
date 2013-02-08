package com.metaframe.cooma.ext6.impl;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext6.InjectExt;

import java.util.List;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Ext6Impl2 implements InjectExt {
    List<String> list;

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public String echo(Config config, String s) {
        throw new UnsupportedOperationException();
    }

}