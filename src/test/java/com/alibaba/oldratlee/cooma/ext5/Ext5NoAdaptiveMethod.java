package com.alibaba.oldratlee.cooma.ext5;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.Extension;

/**
 * @author oldratlee
 */
@Extension("impl1")
public interface Ext5NoAdaptiveMethod {
    String echo(Configs config, String s);
    
    String yell(Configs config, String s);
    
    String bang(Configs config, int i);
}