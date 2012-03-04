package com.oldratlee.cooma.ext3.impl;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.ext3.Ext3;

/**
 * @author oldratlee
 *
 */
public class Ext3Impl2 implements Ext3 {
    public String echo(Configs config, String s) {
        return "Ext3Impl2-echo";
    }
    
    public String yell(Configs config, String s) {
        return "Ext3Impl2-yell";
    }

    public String bang(Configs config, int i) {
        return "bang2";
    }
    
}