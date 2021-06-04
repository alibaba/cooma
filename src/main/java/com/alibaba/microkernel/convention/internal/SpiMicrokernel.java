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
package com.alibaba.microkernel.convention.internal;

import com.alibaba.microkernel.MicrokernelChain;
import com.alibaba.microkernel.convention.Name;
import com.alibaba.microkernel.convention.Order;

import java.util.ServiceLoader;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 基于JAVA的SPI规范加载接口实现类的微内核. (微内核责任链"前置"拦截)
 * <p/>
 * 如果类型为接口，并且值为短名称，如：
 * <pre>{@code
 * protocol=http
 * }</pre>
 * 或：
 * <pre>{@code
 * spiMicrokernel.create(Protocol.class, "http");
 * }</pre>
 * 将在"META-INF/services/接口名"文件中查找名为 "任意包名" + "配置值" + "接口名" 的实现类，如：
 * 在 META-INF/services/com.alibaba.xxx.Protocol 文件中包含：
 * <pre>{@code
 * com.alibaba.xxxx.impl.HttpProtocol
 * }</pre>
 *
 * @author Liang Fei
 * @see ServiceLoader
 */
@Order(300)
public class SpiMicrokernel extends MicrokernelChain {

    private static final Pattern SHORT_NAME = Pattern.compile("\\w+");

    private static boolean isShortName(String value) {
        return SHORT_NAME.matcher(value).matches();
    }

    @Override
    public <T> T create(Class<T> type, String... values) {
        // 如果为短名称
        // 不符合短名称的，交给下一个微内核创建
        if (!type.isInterface() || values.length != 1 || !isShortName(values[0])) {
            return super.create(type, values);
        }

        final String name = values[0];
        final String namePlusTypeName = name + type.getSimpleName(); // 以配置值为前缀 + 接口名为后缀

        // 使用SPI加载器，加载接口实现类
        final ServiceLoader<T> loader = ServiceLoader.load(type);
        // 转换成流
        final Stream<T> stream = StreamSupport.stream(loader.spliterator(), false);
        return stream
                .filter(impl -> checkSpiImpl(name, namePlusTypeName, impl))
                // 找到一个即可
                // TODO 有二义性，可以打一个WARN/ERROR日志说明？
                .findFirst()
                // 如果一个都找不到，抛出异常
                .orElseThrow(() -> new IllegalStateException(
                        "No such SPI implemented class like "
                                + name.substring(0, 1).toUpperCase() + name.substring(1)
                                + " or "
                                + namePlusTypeName.substring(0, 1).toUpperCase() + namePlusTypeName.substring(1)
                                + " in classpath file META-INF/services/" + type.getCanonicalName()));
    }

    private static <T> boolean checkSpiImpl(String name, String namePlusTypeName, T impl) {
        final Class<?> clazz = impl.getClass();
        final String simpleName = clazz.getSimpleName();

        // 忽略大小写 查找 实现类名

        // 以配置值 直接查找 接口实现
        if (simpleName.equalsIgnoreCase(name)) return true;

        // 以配置值为前缀 + 接口名为后缀 查找 接口实现)
        if (simpleName.equalsIgnoreCase(namePlusTypeName)) return true;

        // 查找 Name注解设置的值
        return clazz.isAnnotationPresent(Name.class) && clazz.getAnnotation(Name.class).value().equals(name);
    }

}
