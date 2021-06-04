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
package com.alibaba.microkernel.configuration.internal;

import com.alibaba.microkernel.convention.Order;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 覆盖配置器(配置器责任链"后置"拦截)。
 * <p>
 * 多份配置之间覆盖配置，按优先级取最后一个值（即优先级高的配置值）。如：
 * <p>
 * 第一份配置：
 * <pre>{@code application.name=aaa}</pre>
 * <p>
 * 第二份配置：
 * <pre>{@code application.name=bbb}</pre>
 * <p>
 * 最后的值为：{@code application.name=bbb}，而不是{@code aaa,bbb}
 *
 * @author Liang Fei
 */
@Order(170)
public class OverrideConfigurer extends AbstractOperatorConfigurer {

    @Override
    protected Stream<String> getStream(String key, Stream<String> stream) {
        // 缓存所有值
        List<String> list = stream
                .filter(x -> x != null && x.trim().length() > 0) // FIXME 可能反复在做这样判空，逻辑要收拢
                .collect(Collectors.toList());

        // 找到最后一个值
        return list.size() == 0
                ? Stream.empty()
                : Stream.of(list.get(list.size() - 1));
    }

}
