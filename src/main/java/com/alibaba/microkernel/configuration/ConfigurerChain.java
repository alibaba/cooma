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
package com.alibaba.microkernel.configuration;

import com.alibaba.microkernel.convention.Chain;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 可组成责任链的配置器基类
 *
 * @author Liang Fei
 */
public abstract class ConfigurerChain implements Configurer, Chain<Configurer> {

    /**
     * 责任链下一个配置器
     */
    private Configurer next;

    /**
     * 注入责任链下一个配置器
     *
     * @param next 注入下一个配置器
     * @return 当前配置器自身，用于链式设置调用
     */
    @Override
    public ConfigurerChain setNext(Configurer next) {
        this.next = next;
        return this;
    }

    /**
     * 如果责任链的下一个配置器不为空，则返回下一个配置器的配置值，否则返回空流
     *
     * @param key 配置名
     * @return 下一个配置器的配置值
     */
    @Override
    @Nonnull
    public Stream<String> get(@Nonnull String key) {
        Objects.requireNonNull(key, "Config key can not be null.");
        return next == null ? Stream.empty() : next.get(key);
    }

}
