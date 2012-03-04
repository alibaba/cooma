package com.oldratlee.cooma.ext1.impl;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.ext1.Ext1;

/**
 * @author oldratlee
 *
 */
public class Ext1Impl3 implements Ext1 {
    public String echo(Configs config, String s) {
        return "Ext1Impl3-echo";
    }
    
    public String yell(Configs config, String s) {
        return "Ext1Impl3-yell";
    }

    public String bang(Configs config, int i) {
        return "bang3";
    }
    
}