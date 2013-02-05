package com.metaframe.cooma.ext6_inject;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Config;
import com.metaframe.cooma.Extension;

/**
 * 无Default
 * 
 * @author oldratlee
 */
@Extension
public interface Ext6 {
    @Adaptive
    String echo(Config config, String s);
}