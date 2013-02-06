package com.metaframe.cooma.ext6_inject.impl;

import com.metaframe.cooma.ext1.SimpleExt;
import junit.framework.Assert;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext6_inject.Dao;
import com.metaframe.cooma.ext6_inject.Ext6;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Ext6Impl1 implements Ext6 {
    SimpleExt ext1;
    public Dao obj;
    
    public void setDao(Dao obj){
        Assert.assertNotNull("inject extension instance can not be null", obj);
        Assert.fail();
    }
    
    public void setExt1(SimpleExt ext1) {
        this.ext1 = ext1;
    }

    public String echo(Config config, String s) {
        return "Ext6Impl1-echo-" + ext1.echo(config, s);
    }
    

}