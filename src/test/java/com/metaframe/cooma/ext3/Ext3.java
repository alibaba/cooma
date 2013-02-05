package com.metaframe.cooma.ext3;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Config;
import com.metaframe.cooma.Extension;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
@Extension("impl3")
public interface Ext3 {
    @Adaptive({"key1", "protocol"})
    String echo(Config config, String s);
    
    @Adaptive({"protocol", "key2"})
    String yell(Config config, String s);
    
    String bang(Config config, int i);
}