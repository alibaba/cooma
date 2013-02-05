package com.metaframe.cooma.ext2.impl;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext2.Ext2;
import com.metaframe.cooma.ext2.UrlHolder;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Ext2Impl1 implements Ext2 {
    public String echo(UrlHolder holder, String s) {
        return "Ext2Impl1-echo";
    }
    
    public String yell(Config config, String s) {
        return "Ext2Impl1-yell";
    }
    
    public String bang(Config config, int i) {
        return "bang1";
    }
}