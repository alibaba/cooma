package com.metaframe.cooma.ext5;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.Extension;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
@Extension("impl1")
public interface Ext5NoAdaptiveMethod {
    String echo(Config config, String s);
    
    String yell(Config config, String s);
    
    String bang(Config config, int i);
}