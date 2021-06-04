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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 插入配置器. (配置器责任链"前置"拦截)
 * <p/>
 * 在原有配置的最前增加配置，如：
 * <pre>{@code
 * filters=aaa,bbb
 * filters^=ccc
 * }</pre>
 * 结果为：filters=ccc,aaa,bbb
 *
 * @author Liang Fei
 */
@Order(120)
public class PrependConfigurer extends AbstractOperatorConfigurer {

    private static final String PREPEND = "^";

    @Override
    protected Stream<String> getStream(String key, Stream<String> stream) {
        // 查找要插入的值以及插入的位置映射MAP
        final Map<String, List<String>> index = index(key + PREPEND);
        if (index.size() <= 0) {
            return stream;
        }

        // 将有指定插入位置的值插入在指定位置前面
        final Stream<String> superList = stream
                .flatMap(v -> index.containsKey(v) ? add(index.remove(v), v, false).stream() : Stream.of(v))
                .collect(Collectors.toList())
                .stream();
        // 将没有指定位置以及找不到指定位置的值，插入在最前面
        return index.size() == 0 ? superList :
                Stream.concat(index.values().stream().flatMap(Collection::stream), superList);
    }

}
