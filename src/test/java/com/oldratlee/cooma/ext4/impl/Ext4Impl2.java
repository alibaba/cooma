package com.oldratlee.cooma.ext4.impl;

import java.util.List;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.ext4.Ext4;

/**
 * @author oldratlee
 *
 */
public class Ext4Impl2 implements Ext4 {
    public String echo(Configs config, String s) {
        return "Ext3Impl2-echo";
    }
    
    public String yell(Configs config, String s) {
        return "Ext3Impl2-yell";
    }

    public String bang(Configs config, int i) {
        return "bang2";
    }
    
    public String bark(String name, List<Object> list) {
        return null;
    }
    
}