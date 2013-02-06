package com.metaframe.cooma.ext3;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Config;
import com.metaframe.cooma.Extension;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
@Extension("impl1")
public interface WrappedExt {
    String echo(Config config, String s);

    String yell(Config config, String s);
}