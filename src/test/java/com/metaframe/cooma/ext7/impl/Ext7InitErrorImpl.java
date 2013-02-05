package com.metaframe.cooma.ext7.impl;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext7.Ext7;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
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