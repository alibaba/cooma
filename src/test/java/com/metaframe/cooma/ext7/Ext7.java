package com.metaframe.cooma.ext7;

import com.metaframe.cooma.Adaptive;
import com.metaframe.cooma.Config;
import com.metaframe.cooma.Extension;

/**
 * 用于测试：
 * 扩展点加载失败（如依赖的三方库运行时没有），如扩展点没有用到，则加载不要报错（在使用到时报错）
 * 
 * @author oldratlee
 */
@Extension
public interface Ext7 {
    @Adaptive
    String echo(Config config, String s);
}