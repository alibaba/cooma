package com.oldratlee.cooma.ext1.impl;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.ext1.Ext1;

/**
 * @author oldratlee
 */
public class Ext1Impl1 implements Ext1 {
    public String echo(Configs config, String s) {
        return "Ext1Impl1-echo";
    }
    
    public String yell(Configs config, String s) {
        return "Ext1Impl1-yell";
    }
    
    public String bang(Configs config, int i) {
        return "bang1";
    }
}