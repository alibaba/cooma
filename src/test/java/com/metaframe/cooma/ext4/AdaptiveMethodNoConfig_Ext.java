package com.metaframe.cooma.ext4;

import java.util.List;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Extension;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
@Extension
public interface AdaptiveMethodNoConfig_Ext {
    @Adaptive
    String bark(String name, List<Object> list); // 没有Config参数的方法
}