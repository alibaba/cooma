package com.oldratlee.cooma.ext7.impl;

import com.oldratlee.cooma.Config;
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

    public String echo(Config config, String s) {
        return "";
    }

}