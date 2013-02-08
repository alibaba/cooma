package com.metaframe.cooma.ext4;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Extension;

import java.util.List;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
@Extension
public interface AdaptiveMethodNoConfig_Ext {
    @Adaptive
    String bark(String name, List<Object> list); // 没有Config参数的方法
}