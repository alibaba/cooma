package com.metaframe.cooma.ext5.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext5.NoAdaptiveMethodExt;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Ext5Wrapper1 implements NoAdaptiveMethodExt {
    NoAdaptiveMethodExt instance;
    
    public static AtomicInteger echoCount = new AtomicInteger();
    public static AtomicInteger yellCount = new AtomicInteger();
    public static AtomicInteger bangCount = new AtomicInteger();
    
    public Ext5Wrapper1(NoAdaptiveMethodExt instance) {
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