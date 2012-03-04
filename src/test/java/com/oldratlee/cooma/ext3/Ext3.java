package com.oldratlee.cooma.ext3;

import com.oldratlee.cooma.Adaptive;
import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.Extension;

/**
 * @author oldratlee
 */
@Extension("impl3")
public interface Ext3 {
    @Adaptive({"key1", "protocol"})
    String echo(Configs config, String s);
    
    @Adaptive({"protocol", "key2"})
    String yell(Configs config, String s);
    
    String bang(Configs config, int i);
}