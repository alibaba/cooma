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

import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 基于JAVA的SPI规范加载"全部"接口实现类的微内核. (微内核责任链"前置"拦截)
 * <p/>
 * 如果类型为接口数组类型，并且值为"*"星号，如：
 * <pre>{@code
 * protocols=*
 * }</pre>
 * 或：
 * <pre>{@code
 * allMicrokernel.create(Protocol[].class, "*");
 * }</pre>
 * 将查找"META-INF/services/接口名"文件中的所有实现类，如：
 * 在 META-INF/services/com.alibaba.xxx.Protocol 文件中包含：
 * <pre>{@code
 * com.alibaba.xxx.HttpProtocol
 * com.alibaba.xxx.UdpProtocol
 * }</pre>
 * 则返回结果为：
 * <pre>{@code
 * new Protocol[] { new HttpProtocol(), new UdpProtocol() }
 * }</pre>
 *
 * @author Liang Fei
 * @see ServiceLoader
 * @see SpiMicrokernel
 */
@Order(20)
public class AllMicrokernel extends MicrokernelChain {

    private static final String ALL = "*";

    @Override
    public <T> T create(Class<T> type, String... values) {
        // 数据类型 为 接口 或 接口数组
        //   只有接口才支持 * 的方式 获取所有的扩展实现
        final boolean isInterfaceType = type.isInterface() || (type.isArray() && type.getComponentType().isInterface());
        if (!isInterfaceType) {
            return super.create(type, values);
        }

        // 配置值包含"*"星号
        final boolean isValuesContainsAllValue = Arrays.asList(values).contains(ALL);
        if (!isValuesContainsAllValue) {
            return super.create(type, values);
        }

        // 包含"*"星号，则只能有唯一一个 * 值
        if (values.length != 1) {
            throw new IllegalStateException("values" + Arrays.asList(values) + " contains " + ALL + ", but not single value!");
        }

        // 将所有类全名传给责任链的下一个微内核处理
        return super.create(type, expandAllValueToSpiNames(type));
    }

    private static <T> String[] expandAllValueToSpiNames(Class<T> type) {
        final Class<?> interfaceType = type.isArray() ? type.getComponentType() : type;
        // 使用ServiceLoader加载所有接口实现
        final ServiceLoader loader = ServiceLoader.load(interfaceType);
        // 转换成流
        @SuppressWarnings("unchecked")
        Stream<T> stream = StreamSupport.stream(loader.spliterator(), false);
        // 获取所有实现类的类全名
        return stream.map(Object::getClass)
                .map(implClass -> getSpiName(interfaceType, implClass))
                .toArray(String[]::new);
    }

    /**
     * @see Name
     */
    private static String getSpiName(Class<?> interfaceType, Class<?> implClass) {
        // 如有Name注释，直接读取 Name注释 设置的值
        if (implClass.isAnnotationPresent(Name.class))
            return implClass.getAnnotation(Name.class).value();

        final String className = implClass.getSimpleName();

        // 如果 类名对接口名结尾，则返回 前缀部分
        //   示例：Foo接口 HelloFoo实现类，返回前缀 Hello
        if (className.endsWith(interfaceType.getSimpleName()))
            return className.substring(0, className.length() - interfaceType.getSimpleName().length());

        // 直接返回类名
        return className;
    }

}
