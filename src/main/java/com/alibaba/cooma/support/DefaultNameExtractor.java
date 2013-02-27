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
public class DefaultNameExtractor extends AbstractNameExtractor {
    private final int IS_STRING = 0;
    private final int IS_MAP = 1;
    private final int IS_POJO = 2;

    private int dataType;
    private int pathType = -1;

    private Method getter;
    private List<Method> pojoGetters;

    @Override
    public void init() {
        super.init();

        if (type == String.class) {
            dataType = IS_STRING;
        } else if (Map.class.isAssignableFrom(type)) {
            dataType = IS_MAP;

            if (adaptive.path().length() != 0) {
                throw new IllegalStateException("Not support Adaptive.path to Map parameter type to method " +
                        method.getName() + " of extension " + extension.getName());
            }
        } else {
            dataType = IS_POJO;

            if (adaptive.path().length() > 0) {
                try {
                    String path = adaptive.path();
                    getter = type.getMethod(StringUtils.attribute2Getter(path));
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("no attrib " + adaptive.path() +
                            " for type " + type.getName() + " of method " + method.getName() +
                            " of extension " + extension.getName());
                }
            }

            Class<?> pojoType = type;
            if (getter != null) {
                pojoType = getter.getReturnType();
            }
            if (pojoType.equals(String.class)) {
                pathType = IS_STRING;
            } else if (Map.class.isAssignableFrom(pojoType)) {
                pathType = IS_MAP;
            } else {
                pathType = IS_POJO;
                pojoGetters = new ArrayList<Method>();
                Method[] methods = pojoType.getMethods();
                for (String key : adaptiveKeys) {
                    final String getterName = StringUtils.attribute2Getter(key);
                    Method getter = null;
                    for (Method method : methods) {
                        if (getterName.equals(method.getName()) &&
                                !Modifier.isStatic(method.getModifiers()) &&
                                method.getParameterTypes().length == 0) {
                            getter = method;
                        }
                    }
                    // 如果Key对应的方法不存在，则异常！
                    if (getter == null) {
                        throw new IllegalStateException("No getter method " + getterName +
                                " on parameter type " + pojoType + " to key " + key +
                                " from adaptive keys(" + Arrays.toString(adaptiveKeys));
                    }
                    pojoGetters.add(getter);
                }
            }
        }
    }

    public String getValue(Object argument) {
        if (argument == null) {
            throw new IllegalArgumentException("adaptive " + type.getName() +
                    " argument == null");
        }

        switch (dataType) {
            // 1. 方法参数类型是String，参数值直接作为扩展名称。
            case IS_STRING:
                return (String) argument;
            // 2. 方法参数类型是Map，则提取Map的Value作为扩展名称。
            case IS_MAP:
                return getFromMap(argument, adaptiveKeys);
            // 3. 方法参数作为Pojo，Key作为Pojo上的Get方法，来提取扩展名称。
            case IS_POJO:
                if (pathType == -1) {
                    return getFromPojo(argument, pojoGetters);
                }

                Object attribute = getObject(argument, getter);
                if (attribute == null) {
                    throw new IllegalArgumentException("adaptive " + type.getName() +
                            " argument " + getter.getName() + "() == null");
                }
                switch (pathType) {
                    case IS_STRING:
                        return (String) attribute;
                    case IS_MAP:
                        return getFromMap(attribute, adaptiveKeys);
                    case IS_POJO:
                        return getFromPojo(attribute, pojoGetters);
                }
        }
        return null;
    }

    private static String getFromMap(Object obj, String[] keys) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) obj;
        for (String key : keys) {
            Object value = map.get(key);
            if(value == null) {
                continue;
            }
            return value.toString();
        }
        return null;
    }

    private static String getFromPojo(Object obj, List<Method> getter) {
        for (Method method : getter) {
            try {
                Object ret = method.invoke(obj);
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

    private Object getObject(Object argument, Method getter) {
        try {
            return getter.invoke(argument);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Fail to value via method " +
                    method.getName() + ", cause: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Fail to value via method " +
                    method.getName() + ", cause: " + e.getMessage(), e);
        }
    }
}
