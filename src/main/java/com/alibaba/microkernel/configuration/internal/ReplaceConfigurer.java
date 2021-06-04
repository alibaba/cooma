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
import java.util.Map;
import java.util.stream.Stream;

/**
 * 替换配置器. (配置器责任链"前置"拦截)
 * <p/>
 * 替换原有配置中的某个配置，如：
 * <pre>{@code
 * filters=aaa,bbb,ccc
 * filters~=bbb:mybbb
 * }</pre>
 * 结果为：filters=aaa,mybbb,ccc
 *
 * @author Liang Fei
 */
@Order(110)
public class ReplaceConfigurer extends AbstractOperatorConfigurer {

    private static final String REPLACE = "~";

    @Override
    protected Stream<String> getStream(String key, Stream<String> stream) {
        // 查找被替换值和替换新值的映射MAP
        final Map<String, List<String>> index = index(key + REPLACE);
        // 将被替换值换成新值，新值可以为多个，找不到被替换值的忽略
        return stream.flatMap(v ->
                index.containsKey(v)
                        ? index.get(v).stream()
                        : Stream.of(v));
    }

}
