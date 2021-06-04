/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.microkernel.convention.internal;

import com.alibaba.microkernel.MicrokernelChain;
import com.alibaba.microkernel.convention.Order;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 数组对象创建微内核. (微内核责任链"前置"拦截)
 * <p/>
 * 如果类型为数组类型，则将每个values分别解析为单个实例，并组合成数组返回，如：
 * <pre>{@code
 * arrayMicrokernel.create(String[].class, "aaa", "bbb");
 * }</pre>
 * 则返回结果为:
 * <pre>{@code new String[] {"aaa", "bbb"}}</pre>
 *
 * @author Liang Fei
 */
@Order(100)
public class ArrayMicrokernel extends MicrokernelChain {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> type, String... values) {
        // 不是数组类型，传给责任链的下一个微内核
        if (!type.isArray()) {
            return super.create(type, values);
        }

        // 如果为数组类型，则创建数组
        return (T) createArray((Class<Object[]>) type, values);
    }

    @SuppressWarnings("unchecked")
    private <ComponentType> ComponentType[] createArray(Class<ComponentType[]> type, String... values) {
        final Class<ComponentType> componentType = (Class<ComponentType>) type.getComponentType();

        return Stream.of(values)
                // 获取单个配置值的实例
                .map(v -> super.create(componentType, v))
                // 过滤掉空值
                .filter(Objects::nonNull)
                // 转换为数组
                .toArray(length -> (ComponentType[]) Array.newInstance(componentType, length));
    }

}
