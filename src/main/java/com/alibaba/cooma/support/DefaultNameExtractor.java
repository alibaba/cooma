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
import com.alibaba.cooma.internal.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 缺省AdaptiveInstance调用时，扩展名称提取方法。
 * <p/>
 * <p/>
 * <ol>
 * <li> 有{@link Adaptive}注解的参数是String类型，则参数值直接作为扩展名称。
 * <li> 有{@link Adaptive}注解的参数是Map类型，则提取Map的Value作为扩展名称。
 * <li>
 * </ol>
 *
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 */
// FIXME 每次扩展点调用都要收集参数上的信息，再extract。期望把收集操作提到方法外。
public class DefaultNameExtractor extends AbstractNameExtractor {
    private final int IS_STRING = 0;
    private final int IS_MAP = 1;
    private final int IS_POJO = 2;

    private int dataType;

    private List<Method> pojoGetters;

    @Override
    public void init() {
        super.init();

        if (type == String.class) {
            dataType = IS_STRING;
        } else if (Map.class.isAssignableFrom(type)) {
            dataType = IS_MAP;
        } else {
            dataType = IS_POJO;
            pojoGetters = new ArrayList<Method>();

            Method[] methods = type.getMethods();
            for (String key : adaptiveKeys) {
                final String getterName = StringUtils.attribute2Getter(key);
                Method getter = null;
                // 如果对应的方法不存在，则忽略这个Key
                for (Method method : methods) {
                    if (getterName.equals(method.getName()) &&
                            !Modifier.isStatic(method.getModifiers()) &&
                            method.getParameterTypes().length == 0) {
                        getter = method;
                    }
                }
                if (getter == null) {
                    throw new IllegalStateException("No getter method " + getterName +
                            " on parameter type " + type + " to key " + key +
                            " from adaptive keys(" + Arrays.toString(adaptiveKeys));
                }
                pojoGetters.add(getter);
            }
        }
    }

    @Override
    public String getValue(Object argument) {
        switch (dataType) {
            // 1. 方法参数类型是String，参数值直接作为扩展名称。
            case IS_STRING:
                return (String) argument;
            // 2. 方法参数类型是Map，则提取Map的Value作为扩展名称。
            case IS_MAP:
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) argument;
                for (String key : adaptiveKeys) {
                    String value = map.get(key).toString();
                    if (value != null) {
                        return value;
                    }
                }
                return null;
            // 3. 方法参数作为Pojo，Key作为Pojo上的Get方法，来提取扩展名称。
            case IS_POJO:
                    for (Method method : pojoGetters) {
                        try {
                            Object ret = method.invoke(argument);
                            if (null != ret) {
                                return (String) ret;
                            }
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Fail to value via method " +
                                    method.getName() + ", cause: " + e.getMessage(), e);
                        } catch (InvocationTargetException e) {
                            throw new IllegalStateException("Fail to value via method " +
                                    method.getName() + ", cause: " + e.getMessage(), e);
                        }
                    }
                return null;
        }
        return null;
    }
}
