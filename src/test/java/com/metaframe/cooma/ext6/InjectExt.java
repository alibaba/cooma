package com.metaframe.cooma.ext6;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Config;
import com.metaframe.cooma.Extension;

/**
 * 无Default
 * 
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
@Extension
public interface InjectExt {
    @Adaptive({"key"})
    String echo(Config config, String s);
}