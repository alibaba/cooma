package com.metaframe.cooma.ext2;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Extension;

/**
 * 无Default。
 * 使用ConfigHolder。
 *
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
@Extension
public interface NoDefaultExt {
    @Adaptive
    String echo(ConfigHolder holder, String s);
}