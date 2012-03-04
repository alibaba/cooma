package com.oldratlee.cooma.ext1;

import com.oldratlee.cooma.Adaptive;
import com.oldratlee.cooma.Config;
import com.oldratlee.cooma.Extension;

/**
 * @author oldratlee
 */
@Extension("impl1")
public interface Ext1 {
    @Adaptive
    String echo(Config config, String s);
    
    @Adaptive({"key1", "key2"})
    String yell(Config config, String s);
    
    String bang(Config config, int i);
}