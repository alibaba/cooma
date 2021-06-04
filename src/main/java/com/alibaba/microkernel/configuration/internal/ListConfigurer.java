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

import com.alibaba.microkernel.configuration.ConfigurerChain;
import com.alibaba.microkernel.convention.Order;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 列表配置器. (配置器责任链"后置"拦截)
 * <p/>
 * 以逗号分隔多个值，如：
 * <pre>{@code
 * protocols=http,udp
 * }</pre>
 *
 * @author Liang Fei
 */
@Order(160)
public class ListConfigurer extends ConfigurerChain {

    private static final Pattern LIST = Pattern.compile("\\s*,\\s*");

    @Nonnull
    @Override
    public Stream<String> get(@Nonnull String key) {
        // 以逗号为分隔符展开为多个值
        return super.get(key).flatMap(LIST::splitAsStream);
    }

}
