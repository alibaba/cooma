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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 * @since 0.3.0
 */
public abstract class AbstractNameExtractor implements NameExtractor {
    protected Method method;

    protected Class<?> extension;

    protected int adaptiveArgumentIndex;
    protected Class<?> parameterType;
    protected Adaptive adaptive;

    protected String[] adaptiveKeys;

    public void setMethod(Method method) {
        this.method = method;
        this.extension = method.getDeclaringClass();

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            for (Annotation a : annotations) {
                if (a instanceof Adaptive) {
                    adaptiveArgumentIndex = i;
                    parameterType = method.getParameterTypes()[i];
                    adaptive = (Adaptive) a;
                    break;
                }
            }
        }
    }

    public final void init() {
        String[] keys = adaptive.value();
        if (keys.length == 0) {
            // 没有设置Key，则使用“扩展点接口名的点分隔”作为Key
            keys = new String[]{StringUtils.toDotSpiteString(extension.getSimpleName())};
        }
        adaptiveKeys = keys;

        doInit();
    }

    protected abstract void doInit();

    public abstract String extract(Object argument);
}
