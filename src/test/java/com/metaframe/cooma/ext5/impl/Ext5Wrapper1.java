package com.metaframe.cooma.ext5.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext5.Ext5NoAdaptiveMethod;

/**
 * @author oldratlee
 */
public class Ext5Wrapper1 implements Ext5NoAdaptiveMethod {
    Ext5NoAdaptiveMethod instance;
    
    public static AtomicInteger echoCount = new AtomicInteger();
    public static AtomicInteger yellCount = new AtomicInteger();
    public static AtomicInteger bangCount = new AtomicInteger();
    
    public Ext5Wrapper1(Ext5NoAdaptiveMethod instance) {
        this.instance = instance;
    }
    
    public String echo(Config config, String s) {
        echoCount.incrementAndGet();
        return instance.echo(config, s);
    }

    public String yell(Config config, String s) {
        yellCount.incrementAndGet();
        return instance.yell(config, s);
    }

    public String bang(Config config, int i) {
        bangCount.incrementAndGet();
        return instance.bang(config, i);
    }
}