package com.oldratlee.cooma.ext6_inject;

import com.oldratlee.cooma.Adaptive;
import com.oldratlee.cooma.Config;
import com.oldratlee.cooma.Extension;

/**
 * 无Default
 * 
 * @author oldratlee
 */
@Extension
public interface Ext6 {
    @Adaptive
    String echo(Config config, String s);
}