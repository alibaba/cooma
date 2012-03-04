package com.oldratlee.cooma.ext2.impl;

import com.oldratlee.cooma.Config;
import com.oldratlee.cooma.ext2.Ext2;
import com.oldratlee.cooma.ext2.UrlHolder;

/**
 * @author oldratlee
 *
 */
public class Ext2Impl3 implements Ext2 {
    public String echo(UrlHolder holder, String s) {
        return "Ext2Impl3-echo";
    }
    
    public String yell(Config config, String s) {
        return "Ext2Impl3-yell";
    }

    public String bang(Config config, int i) {
        return "bang3";
    }
    
}