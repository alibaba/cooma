package com.oldratlee.cooma.ext4.impl;

import java.util.List;

import com.oldratlee.cooma.Config;
import com.oldratlee.cooma.ext4.Ext4;

/**
 * @author oldratlee
 *
 */
public class Ext4Impl2 implements Ext4 {
    public String echo(Config config, String s) {
        return "Ext3Impl2-echo";
    }
    
    public String yell(Config config, String s) {
        return "Ext3Impl2-yell";
    }

    public String bang(Config config, int i) {
        return "bang2";
    }
    
    public String bark(String name, List<Object> list) {
        return null;
    }
    
}