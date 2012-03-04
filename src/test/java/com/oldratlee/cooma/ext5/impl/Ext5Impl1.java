package com.oldratlee.cooma.ext5.impl;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.ext5.Ext5NoAdaptiveMethod;

/**
 * @author oldratlee
 */
public class Ext5Impl1 implements Ext5NoAdaptiveMethod {
    public String echo(Configs config, String s) {
        return "Ext5Impl1-echo";
    }

    public String yell(Configs config, String s) {
        return "Ext5Impl1-yell";
    }

    public String bang(Configs config, int i) {
        return "impl1";
    }
}