package com.oldratlee.cooma.ext2;

import com.oldratlee.cooma.Adaptive;
import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.Extension;

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
    String yell(Configs config, String s);
    
    String bang(Configs config, int i);
}