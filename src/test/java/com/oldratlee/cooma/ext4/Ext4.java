package com.oldratlee.cooma.ext4;

import java.util.List;

import com.oldratlee.cooma.Adaptive;
import com.oldratlee.cooma.Extension;

/**
 * @author oldratlee
 */
@Extension("impl1")
public interface Ext4 {
    @Adaptive
    String bark(String name, List<Object> list); // 没有URL参数的方法
}