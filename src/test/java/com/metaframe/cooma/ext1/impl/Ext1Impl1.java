package com.metaframe.cooma.ext1.impl;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext1.Ext1;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Ext1Impl1 implements Ext1 {
    public String echo(Config config, String s) {
        return "Ext1Impl1-echo";
    }
    
    public String yell(Config config, String s) {
        return "Ext1Impl1-yell";
    }
    
    public String bang(Config config, int i) {
        return "bang1";
    }
}