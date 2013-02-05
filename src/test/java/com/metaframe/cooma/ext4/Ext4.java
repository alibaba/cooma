package com.metaframe.cooma.ext4;

import java.util.List;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Extension;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
@Extension("impl1")
public interface Ext4 {
    @Adaptive
    String bark(String name, List<Object> list); // 没有Config参数的方法
}