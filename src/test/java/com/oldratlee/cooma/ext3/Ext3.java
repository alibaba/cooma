package com.oldratlee.cooma.ext3;

import com.oldratlee.cooma.Adaptive;
import com.oldratlee.cooma.Config;
import com.oldratlee.cooma.Extension;

/**
 * @author oldratlee
 */
@Extension("impl3")
public interface Ext3 {
    @Adaptive({"key1", "protocol"})
    String echo(Config config, String s);
    
    @Adaptive({"protocol", "key2"})
    String yell(Config config, String s);
    
    String bang(Config config, int i);
}