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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * 操作符配置器基类.
 * <p/>
 * 对配置进行操作转化，将过滤操作符本身，防止操作符间循环处理
 *
 * @author Liang Fei
 */
public abstract class AbstractOperatorConfigurer extends ConfigurerChain {

    protected static final String INDEX = ":";

    @Nonnull
    @Override
    public Stream<String> get(@Nonnull String key) {
        Stream<String> stream = super.get(key);
        if (key.length() > 0) {
            if (isSimpleKey(key)) {
                return getStream(key, stream);
            }
        }
        return stream;
    }

    // 简单的Key，不是一个操作符Key（后面会带有 ^+-~ ）
    private static boolean isSimpleKey(String key) {
        char ch = key.charAt(key.length() - 1);
        return (ch >= '0' && ch <= '9')
                || (ch >= 'A' && ch <= 'Z')
                || (ch >= 'a' && ch <= 'z');
    }

    protected abstract Stream<String> getStream(String key, Stream<String> stream);

    protected Map<String, List<String>> index(String key) {
        Stream<String> appends = super.get(key);
        Map<String, List<String>> map = new TreeMap<>();

        appends.forEach(append -> {
            int i = append.indexOf(INDEX);
            String k;
            String v;
            if (i >= 0) {
                k = append.substring(0, i).trim();
                v = append.substring(i + 1).trim();
            } else {
                k = "";
                v = append.trim();
            }
            if (v.length() > 0) {
                List<String> l = map.computeIfAbsent(k, k1 -> new ArrayList<>());
                l.add(v);
            }
        });
        return map;
    }

    protected static List<String> add(List<String> list, String value, boolean first) {
        if (first) {
            list.add(0, value);
        } else {
            list.add(value);
        }
        return list;
    }

}
