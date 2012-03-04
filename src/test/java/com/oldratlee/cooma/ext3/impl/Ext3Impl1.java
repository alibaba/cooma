package com.oldratlee.cooma.ext3.impl;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.ext3.Ext3;

/**
 * @author oldratlee
 */
public class Ext3Impl1 implements Ext3 {
    public String echo(Configs config, String s) {
        return "Ext3Impl3-echo";
    }
    
    public String yell(Configs config, String s) {
        return "Ext3Impl3-yell";
    }
    
    public String bang(Configs config, int i) {
        return "bang1";
    }
}