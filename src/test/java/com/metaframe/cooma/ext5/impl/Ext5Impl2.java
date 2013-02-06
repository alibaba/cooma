package com.metaframe.cooma.ext5.impl;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext5.NoAdaptiveMethodExt;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Ext5Impl2 implements NoAdaptiveMethodExt {
    public String echo(Config config, String s) {
        return "Ext5Impl2-echo";
    }

    public String yell(Config config, String s) {
        return "Ext5Impl2-yell";
    }

    public String bang(Config config, int i) {
        return "impl2";
    }
}