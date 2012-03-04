package com.oldratlee.cooma.ext6_inject.impl;

import junit.framework.Assert;

import com.oldratlee.cooma.Config;
import com.oldratlee.cooma.ext1.Ext1;
import com.oldratlee.cooma.ext6_inject.Dao;
import com.oldratlee.cooma.ext6_inject.Ext6;

/**
 * @author oldratlee
 */
public class Ext6Impl1 implements Ext6 {
    Ext1 ext1;
    public Dao obj;
    
    public void setDao(Dao obj){
        Assert.assertNotNull("inject extension instance can not be null", obj);
        Assert.fail();
    }
    
    public void setExt1(Ext1 ext1) {
        this.ext1 = ext1;
    }

    public String echo(Config config, String s) {
        return "Ext6Impl1-echo-" + ext1.echo(config, s);
    }
    

}