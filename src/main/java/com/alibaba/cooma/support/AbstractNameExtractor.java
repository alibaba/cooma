/*
 * Copyright 2012-2013 Cooma Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cooma.support;

import com.alibaba.cooma.Adaptive;
import com.alibaba.cooma.NameExtractor;
import com.alibaba.cooma.internal.utils.StringUtils;

import java.lang.reflect.Method;

/**
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 * @since 0.3.0
 */
public abstract class AbstractNameExtractor implements NameExtractor {
    protected Class<?> extension;
    protected Method method;
    protected Class<?> type;
    protected Adaptive adaptive;

    protected String[] adaptiveKeys;

    public void setExtension(Class<?> extension) {
        this.extension = extension;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setParameterType(Class<?> type) {
        this.type = type;
    }

    public void setAdaptive(Adaptive adaptive) {
        this.adaptive = adaptive;
        this.adaptiveKeys = adaptive.value();
    }

    public void init() {
        String[] keys = adaptive.value();
        if (keys.length == 0) {
            // 没有设置Key，则使用“扩展点接口名的点分隔”作为Key
            keys = new String[]{StringUtils.toDotSpiteString(extension.getSimpleName())};
        }
        adaptiveKeys = keys;
    }

    public abstract String extract(Object argument);
}
