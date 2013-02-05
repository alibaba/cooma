package com.metaframe.cooma.ext3.impl;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext3.Ext3;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Ext3Impl3 implements Ext3 {
    public String echo(Config config, String s) {
        return "Ext3Impl3-echo";
    }
    
    public String yell(Config config, String s) {
        return "Ext3Impl3-yell";
    }

    public String bang(Config config, int i) {
        return "bang3";
    }
    
}