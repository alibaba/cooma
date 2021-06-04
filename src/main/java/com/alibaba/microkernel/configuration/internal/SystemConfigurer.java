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
 * Java系统-D参数配置器（配置器责任链"后置"拦截）。
 * <p>
 * 当设置前缀时（{@link #setPrefix(String)}），读取的-D参数对应的是 前缀 加 Key，防止配置名与其它框架冲突。
 * <p>
 * 可用于系统运维时，替换代码中的配置，优先级高于配置文件。
 * <p>
 * 配置如：
 * <pre>{@code
 * java -Dxxx=xxx
 * java -Ddefault.timeout=1000
 * }</pre>
 *
 * @author Liang Fei
 */
@Order(210)
public class SystemConfigurer extends ConfigurerChain {

    private String prefix;

    /**
     * 设置系统配置前缀，
     *
     * @param prefix 配置前缀
     * @return 当前配置器本身
     */
    public SystemConfigurer setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Nonnull
    @Override
    public Stream<String> get(@Nonnull String key) {
        String value = null;

        // 如果有前缀，先读取前缀+配置键的值，防止配置名与其它框架冲突
        if (prefix != null && prefix.length() > 0)
            value = getProperty(prefix + key);

        // FIXME 直接再去读Key 是否合适？重新了引入冲突风险！
        // 如果值为空，加载系统属性值
        if (value == null || value.length() == 0)
            value = getProperty(key);

        // 将系统属性值转换为流
        Stream<String> stream =
                value == null || value.length() == 0
                        ? Stream.empty()
                        : Stream.of(value);

        // 连接父节点配置
        return Stream.concat(super.get(key), stream);
    }

    protected String getProperty(String key) {
        // 读取命令行-D参数
        return System.getProperty(key);
    }

}
