package com.metaframe.cooma.ext8;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Config;
import com.metaframe.cooma.Extension;

/**
 * 用于测试：
 * 非法的扩展点名
 *
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
@Extension("invalid-name&")
public interface InvalidNameExt {
    @Adaptive("key")
    String echo(Config config, String s);
}