package com.oldratlee.cooma.ext3.impl;

import com.oldratlee.cooma.Config;
import com.oldratlee.cooma.ext3.Ext3;

/**
 * @author oldratlee
 *
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