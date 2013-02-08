package com.metaframe.cooma.ext1;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Config;
import com.metaframe.cooma.Extension;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
@Extension("impl1")
public interface SimpleExt {
    // 没有使用key的@Adaptive ！
    @Adaptive
    String echo(Config config, String s);

    @Adaptive({"key1", "key2"})
    String yell(Config config, String s);

    // 无@Adaptive ！
    String bang(Config config, int i);
}