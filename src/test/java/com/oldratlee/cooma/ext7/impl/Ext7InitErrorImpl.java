package com.oldratlee.cooma.ext7.impl;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.ext7.Ext7;

/**
 * @author oldratlee
 */
public class Ext7InitErrorImpl implements Ext7 {
    
    static {
        if(true) {
            throw new RuntimeException("intended!");
        }
    }

    public String echo(Configs config, String s) {
        return "";
    }

}