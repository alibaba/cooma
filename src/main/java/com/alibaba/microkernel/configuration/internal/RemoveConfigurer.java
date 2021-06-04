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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 减号配置器. (配置器责任链"前置"拦截)
 * <p/>
 * 在原有配置的基础上移除配置，如：
 * <pre>{@code
 * filters=aaa,bbb,ccc
 * filters-=bbb
 * }</pre>
 * 结果为：filters=aaa,ccc
 *
 * @author Liang Fei
 */
@Order(100)
public class RemoveConfigurer extends AbstractOperatorConfigurer {

    private static final String REMOVE = "-";

    @Override
    protected Stream<String> getStream(String key, Stream<String> stream) {
        // 查找要移除的值的集合
        final Set<String> removeSet = super.get(key + REMOVE).collect(Collectors.toSet());
        // 过滤掉包含在集合中的值
        return stream.filter(v -> !removeSet.contains(v));
    }

}
