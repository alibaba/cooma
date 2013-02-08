package com.metaframe.cooma.ext1.impl;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext1.SimpleExt;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class SimpleExtImpl2 implements SimpleExt {
    public String echo(Config config, String s) {
        return "Ext1Impl2-echo";
    }

    public String yell(Config config, String s) {
        return "Ext1Impl2-yell";
    }

    public String bang(Config config, int i) {
        return "bang2";
    }

}