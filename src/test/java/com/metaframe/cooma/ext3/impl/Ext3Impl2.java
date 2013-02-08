package com.metaframe.cooma.ext3.impl;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext3.WrappedExt;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Ext3Impl2 implements WrappedExt {
    public String echo(Config config, String s) {
        return "Ext3Impl2-echo";
    }

    public String yell(Config config, String s) {
        return "Ext3Impl2-yell";
    }

    public String bang(Config config, int i) {
        return "impl2";
    }
}