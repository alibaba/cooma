package com.metaframe.cooma.ext3.impl;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext3.WrappedExt;
import com.metaframe.cooma.ext5.NoAdaptiveMethodExt;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Ext3Wrapper1 implements WrappedExt {
    WrappedExt instance;
    
    public static AtomicInteger echoCount = new AtomicInteger();
    public static AtomicInteger yellCount = new AtomicInteger();

    public Ext3Wrapper1(WrappedExt instance) {
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
}