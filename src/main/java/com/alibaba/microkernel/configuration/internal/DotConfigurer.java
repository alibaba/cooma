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
import java.util.stream.Stream;

/**
 * 点号分隔命名配置器（配置器责任链"前置"拦截）。
 * 将大写驼峰命名的{@code key}转换为点号分隔小写。
 * <p>
 * 如，将{@code StreamProtocol=http} 转为 {@code stream.protocol=http}。
 *
 * @author Liang Fei
 */
@Order(10)
public class DotConfigurer extends ConfigurerChain {

    private static final char DOT = '.';

    @Nonnull
    @Override
    public Stream<String> get(@Nonnull String key) {
        if (key.trim().length() == 0)
            return Stream.empty();

        return super.get(convert2DotCase(key));
    }

    private static String convert2DotCase(String key) {
        // 转换驼峰命名KEY为点号分隔命名
        StringBuilder builder = new StringBuilder(key.length() + 4);
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            // 如果是大写字母
            if (Character.isUpperCase(c)) {
                // 非第一个字母，前面加上点号
                if (i > 0 && key.charAt(i - 1) != DOT)
                    builder.append(DOT);
                // 转成小写字母
                builder.append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
