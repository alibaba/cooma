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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 引用配置器. (配置器责任链"后置"拦截)
 * <p/>
 * 引用另一个配置的值，如：
 * <pre>{@code
 * input.encoding=UTF-8
 * output.encoding=$input.encoding
 * }</pre>
 * 结果为：output.encoding=UTF-8
 *
 * @author Liang Fei
 */
@Order(140)
public class ReferenceConfigurer extends ConfigurerChain {

    private static final String REFERENCE = "$";

    private static final String INDEX = ":";

    @Nonnull
    @Override
    public Stream<String> get(@Nonnull String key) {
        return flatGet(key, new ArrayList<>());
    }

    private Stream<String> flatGet(final String key, final List<String> history) {
        if (history.contains(key)) // 循环引用
            throw new IllegalStateException("The configuration key \"" + key + "\" circular reference chain: "
                    + String.join(" -> ", history) + " -> " + key);

        history.add(key); // 加入引用链记录

        // 读取引用值，引用值可以为多个值
        return super.get(key)
                .flatMap(value -> dereference(value, history));
    }

    /**
     * 把 valueWithReference 中的 Reference($foo) 都解成具体值，即 dereference（解引用）
     *
     * @param valueWithReference 可能包含 Reference
     */
    private Stream<String> dereference(final String valueWithReference, final List<String> history) {
        final int idx = valueWithReference.indexOf(INDEX);
        final int length = REFERENCE.length();

        // Head-tail/Index Style，即包含『:』（index），$head:$tail，在操作符Configurer中使用
        if (idx >= 0) {
            // 解析替换值的冒号分隔 HEAD:TAIL
            final String headPart = valueWithReference.substring(0, idx).trim(); // TODO 关于
            final String tailPart = valueWithReference.substring(idx + 1).trim();

            final boolean headIsReference = headPart.startsWith(REFERENCE);
            final boolean tailIsReference = tailPart.startsWith(REFERENCE);

            final String headName = headPart.substring(REFERENCE.length());
            final String tailName = tailPart.substring(REFERENCE.length());

            if (headIsReference) {
                if (tailIsReference) {
                    // head和tail都为引用，都做解引用，如：filters~=$trace:$mytrace
                    return flatGet(headName, history)
                            .flatMap(k -> flatGet(tailName, new ArrayList<>(history))
                                    .map(v -> k + INDEX + v)
                            );
                } else {
                    // 只有head为引用，如：filters~=$trace:mytrace
                    return flatGet(headName, history)
                            .map(k -> k + INDEX + tailPart);
                }
            } else if (tailIsReference) {
                // 只有tail为引用，如：filters~=trace:$mytrace
                return flatGet(tailName, history)
                        .map(v -> headPart + INDEX + v);
            }
        }
        // HEAD only style
        else if (valueWithReference.startsWith(REFERENCE)) {
            // 递归读取引用值，可间接引用
            final String headName = valueWithReference.substring(length);
            return flatGet(headName, history);
        }

        // 将引用值转换成流
        return Stream.of(valueWithReference);
    }
}
