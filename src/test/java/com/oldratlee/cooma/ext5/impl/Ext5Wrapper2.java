package com.oldratlee.cooma.ext5.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.ext5.Ext5NoAdaptiveMethod;

/**
 * @author oldratlee
 */
public class Ext5Wrapper2 implements Ext5NoAdaptiveMethod {
    Ext5NoAdaptiveMethod instance;
    
    public static AtomicInteger echoCount = new AtomicInteger();
    public static AtomicInteger yellCount = new AtomicInteger();
    public static AtomicInteger bangCount = new AtomicInteger();
    
    public Ext5Wrapper2(Ext5NoAdaptiveMethod instance) {
        this.instance = instance;
    }
    
    public String echo(Configs config, String s) {
        echoCount.incrementAndGet();
        return instance.echo(config, s);
    }

    public String yell(Configs config, String s) {
        yellCount.incrementAndGet();
        return instance.yell(config, s);
    }

    public String bang(Configs config, int i) {
        bangCount.incrementAndGet();
        return instance.bang(config, i);
    }
}