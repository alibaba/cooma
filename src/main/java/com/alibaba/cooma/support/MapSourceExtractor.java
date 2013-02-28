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

import com.alibaba.cooma.internal.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 缺省AdaptiveInstance调用时，扩展名称提取方法。
 * <p/>
 * <ol>
 * <li> 有{@link com.alibaba.cooma.Adaptive}注解的参数是String类型，则参数值直接作为扩展名称。
 * <li> 有{@link com.alibaba.cooma.Adaptive}注解的参数是Map类型，则提取Map的Value作为扩展名称。
 * <li>
 * </ol>
 *
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 * @since 0.3.0
 */
public class MapSourceExtractor extends AbstractNameExtractor {

    private FromAttribute fromAttribute = null;
    private Method fromGetter = null;

    @Override
    protected void doInit() {Annotation[] annotations = method.getParameterAnnotations()[adaptiveArgumentIndex];
        for (Annotation a : annotations) {
            if (a instanceof FromAttribute) {
                if (fromAttribute != null) {
                    throw new IllegalStateException("at most 1 " + FromAttribute.class.getSimpleName() +
                            " annotation is allowed!");
                }
                fromAttribute = (FromAttribute) a;
                if (Map.class.isAssignableFrom(parameterType)) {
                    throw new IllegalStateException("annotation " +
                            FromAttribute.class.getSimpleName() + " can only be used on POJO!");
                }
                String getter = StringUtils.attribute2Getter(fromAttribute.value());
                try {
                    fromGetter = parameterType.getMethod(getter);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("No " + getter + " method for parameter type " +
                            parameterType.getName() + " on method " + method.getName() +
                            " of extension " + extension.getName());
                }
            }
        }
    }

    public String extract(Object argument) {
        if (argument == null) {
            throw new IllegalArgumentException("adaptive " + parameterType.getName() +
                    " argument == null");
        }

        Object sourceObject = argument;
        if (fromAttribute != null) {
            try {
                sourceObject = fromGetter.invoke(sourceObject);
                if(null == sourceObject) {
                    throw new IllegalArgumentException("adaptive " + parameterType.getName() +
                            " argument " + fromGetter.getName() + "() == null");
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Fail to get attribute " +
                        fromGetter.getName() + ", cause: " + e.getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Fail to get attribute " +
                        fromGetter.getName() + ", cause: " + e.getMessage(), e);
            }
        }
        return getFromMap(sourceObject, adaptiveKeys);
    }

    private static String getFromMap(Object obj, String[] keys) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) obj;
        for (String key : keys) {
            Object value = map.get(key);
            if (value == null) {
                continue;
            }
            return value.toString();
        }
        return null;
    }
}
