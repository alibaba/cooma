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
import com.alibaba.microkernel.configuration.Configurer;
import com.alibaba.microkernel.convention.Order;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 配置加载微内核. (微内核责任链"前置"拦截)
 * <p>
 * 如果配置了：/META-INF/microkernel.properites
 * <pre>{@code
 * hello=world
 * }</pre>
 * 则：
 * <pre>{@code
 * configMicrokernel.create(String.class, "hello");
 * }</pre>
 * 返回结果为：world
 * <p/>
 * 支持多key查找，将从第一个key开始，直到查找到有值的key为止：
 * <pre>{@code
 * configMicrokernel.create(String.class, "system.prefix", "prefix");
 * }</pre>
 * 优先查找：system.prefix=xxx，如果没有配置或值为空，则查找：prefix=xxx
 *
 * @author Liang Fei
 * @see Configurer
 */
@Order(10)
public class ConfigMicrokernel extends MicrokernelChain {

    // 配置加载器
    private volatile Configurer configurer;

    /**
     * 设置配置加载器. (整个Microkernel实现，有且仅有此处和Configurer进行衔接)
     *
     * @param configurer 配置加载器
     * @return 当前微内核自身，用于链式设置调用
     */
    public ConfigMicrokernel setConfigurer(Configurer configurer) {
        this.configurer = configurer;
        return this;
    }

    @Override
    public <T> T create(Class<T> type, String... keys) {
        keys = normalizeKeys(keys);
        if (keys.length == 0) {
            // 如果没有配置键，以类型名为配置键，如果是数组，取其元素类型名
            Class<?> componentType = type.isArray() ? type.getComponentType() : type;
            keys = new String[]{componentType.getSimpleName()};
        }

        // 逐个查找配置键所对应的配置值，找到一个有值的为止
        String[] values = Stream.of(keys)
                // 从配置器中读取配置的值
                .map(key -> configurer.get(key).toArray(String[]::new))
                // 过滤掉没有值的配置
                .filter(value -> value.length > 0)
                // 查找到一个有配置值的配置即可
                .findFirst()
                // 否则传入空值
                .orElse(null);

        // 如果没有配置，直接返回空
        if (values == null || values.length == 0)
            return null;

        // 将配置值传给责任链下一个微内核
        T object = super.create(type, values);

        // 如果创建的实例为用户自定义配置器，更新当前配置器
        if (object instanceof Configurer)
            configurer = (Configurer) object;

        // 返回实例
        return object;
    }

    @Nonnull
    private static String[] normalizeKeys(String[] keys) {
        if (keys == null || keys.length == 0) {
            return new String[0];
        }
        return Arrays.stream(keys).filter(Objects::nonNull) // not null
                .filter(x -> x.trim().length() > 0) // not blank
                .toArray(String[]::new);
    }

}
