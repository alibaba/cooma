package com.metaframe.cooma.ext2;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Config;
import com.metaframe.cooma.Extension;

/**
 * 无Default
 * 
 * @author oldratlee
 */
@Extension
public interface Ext2 {
    @Adaptive
    String echo(UrlHolder holder, String s);
    
    @Adaptive({"key1", "protocol"})
    String yell(Config config, String s);
    
    String bang(Config config, int i);
}