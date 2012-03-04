package com.oldratlee.cooma.ext5;

import com.oldratlee.cooma.Config;
import com.oldratlee.cooma.Extension;

/**
 * @author oldratlee
 */
@Extension("impl1")
public interface Ext5NoAdaptiveMethod {
    String echo(Config config, String s);
    
    String yell(Config config, String s);
    
    String bang(Config config, int i);
}