package com.oldratlee.cooma.ext1;

import com.oldratlee.cooma.Adaptive;
import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.Extension;

/**
 * @author oldratlee
 */
@Extension("impl1")
public interface Ext1 {
    @Adaptive
    String echo(Configs config, String s);
    
    @Adaptive({"key1", "key2"})
    String yell(Configs config, String s);
    
    String bang(Configs config, int i);
}