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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Jerry
 * Date: 13-2-27
 * Time: 下午4:53
 * To change this template use File | Settings | File Templates.
 */ // FIXME 每次扩展点调用都要收集参数上的信息，再extract。期望把收集操作提到方法外。
public class DefaultNameExtractor implements NameExtractor {
    public String getValue(Class<?> type, Object argument, Adaptive adaptive) {
        // 1. 方法参数类型是String，参数值直接作为扩展名称。
        if (type == String.class) return (String) argument;

        final String[] keys = adaptive.value();

        // 2. 方法参数类型是Map，则提取Map的Value作为扩展名称。
        if (Map.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) argument;
            for (String key : keys) {
                String value = map.get(key).toString();
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        // 3. 方法参数作为Pojo，Key作为Pojo上的Get方法，来提取扩展名称。
        Method[] methods = type.getMethods();
        for (String key : keys) {
            String getterName = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
            // 如果对应的方法不存在，则忽略这个Key
            for (Method method : methods) {
                if (getterName.equals(method.getName()) &&
                        !Modifier.isStatic(method.getModifiers()) &&
                        method.getParameterTypes().length == 0) {
                    try {
                        Object ret = method.invoke(argument);
                        if (null != ret) {
                            return (String) ret;
                        }
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Fail to value from key(" +
                                key + ") by method " + method.getName() + ", cause: " + e.getMessage(), e);
                    } catch (InvocationTargetException e) {
                        throw new IllegalStateException("Fail to value from key(" +
                                key + ") by method " + method.getName() + ", cause: " + e.getMessage(), e);
                    }
                }
            }
        }
        return null;
    }
}
