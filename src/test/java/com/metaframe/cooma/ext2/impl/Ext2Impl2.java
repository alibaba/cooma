package com.metaframe.cooma.ext2.impl;

import com.metaframe.cooma.ext2.ConfigHolder;
import com.metaframe.cooma.ext2.NoDefaultExt;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class Ext2Impl2 implements NoDefaultExt {
    public String echo(ConfigHolder holder, String s) {
        return "Ext2Impl2-echo";
    }
}