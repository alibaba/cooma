package com.metaframe.cooma.ext6.impl;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext1.SimpleExt;
import com.metaframe.cooma.ext6.Dao;
import com.metaframe.cooma.ext6.InjectExt;
import junit.framework.Assert;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Ext6Impl1 implements InjectExt {
    SimpleExt simpleExt;
    public Dao obj;

    public void setDao(Dao obj) {
        this.obj = obj;

        Assert.assertNotNull("inject extension instance can not be null", obj);
        Assert.fail();
    }

    public void setSimpleExt(SimpleExt simpleExt) {
        this.simpleExt = simpleExt;
    }

    public String echo(Config config, String s) {
        return "Ext6Impl1-echo-" + simpleExt.echo(config, s);
    }
}