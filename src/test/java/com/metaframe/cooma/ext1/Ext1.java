package com.metaframe.cooma.ext1;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Config;
import com.metaframe.cooma.Extension;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
@Extension("impl1")
public interface Ext1 {
    @Adaptive
    String echo(Config config, String s);
    
    @Adaptive({"key1", "key2"})
    String yell(Config config, String s);
    
    String bang(Config config, int i);
}