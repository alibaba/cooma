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

import com.alibaba.microkernel.convention.Order;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 责任链模式微内核(微内核责任链"前置"拦截)。
 * <p/>
 * 采用责任链模式，将多个实例通过{@code setNext()}方法组装成单条链.
 * <p/>
 * 比如配置：
 * <pre>{@code
 * filter=filter1,filter2,filter3
 * }</pre>
 * <p>
 * 相当于：
 * <pre>{@code
 * filter1.setNext(filter2);
 * filter2.setNext(filter3);
 * return filter1;
 * }</pre>
 *
 * @author Liang Fei
 */
@Order(30)
public class AopMicrokernel extends AbstractMethodMicrokernel {

    private static final String NEXT = "setNext";

    private static final String ORDER = "order";

    @Override
    public <T> T create(Class<T> type, String... values) {
        // 如果是单个接口注入，却有多个值，采用责任链模式将多个值组装成单个值
        // 否则跳过当前微内核
        if (!type.isInterface() || values.length <= 1) {
            return super.create(type, values);
        }

        // 获取的类型 的 实例数组
        T[] array = super.create(getArrayTypeOf(type), values);

        // 如果列表为空，返回空
        if (array == null || array.length == 0)
            return null;
        // 如果只有一个值，直接返回
        if (array.length == 1)
            return array[0];

        // 如果列表超过一个值，组成链
        return chainSpiInstances(array);
    }

    /**
     * 调用next方法 来 组链，即 上一个SPI实例 通过 next方法 设置到下一个SPI实例中，
     * 返回的是 最后一个SPI实例
     */
    private static <T> T chainSpiInstances(T[] array) {
        List<T> list = Arrays.asList(array);

        // 按order()返回排序值，逆序方便归并
        list.sort(Comparator.comparingInt(AopMicrokernel::order).reversed());

        return list.stream().reduce(AopMicrokernel::accumulator).get();
    }

    private static int order(Object object) {
        final Order orderAnnotation = object.getClass().getAnnotation(Order.class);
        Method orderMethod = findMethod(object, ORDER);

        // 同时声明了注解和方法，报错提醒用户冲突
        if (orderAnnotation != null && orderMethod != null) {
            throw new IllegalStateException("\"@Order()\" annotation and \"int order()\" method cannot be declared in the \""
                    + object.getClass().getCanonicalName() + "\" class at the same time");
        }

        // 如果@Order(123)注解，读取注解值用于排序
        if (orderAnnotation != null) {
            return orderAnnotation.value();
        }

        // 如果有int order()方法，读取其值用于排序
        Object order = invokeMethod(object, orderMethod);
        if (order instanceof Number)
            return ((Number) order).intValue();

        // Order注解、order方法都没有，下面是 设置缺省order值

        // 如果没有setNext，表示该节点需放到最后
        if (findMethod(object, NEXT, object) == null)
            return Integer.MAX_VALUE;

        // 默认放到最前
        return 0;
    }

    private static <T> T accumulator(T result, T element) {
        // 查找责任链的setNext归并方法
        final Method method = findMethod(element, NEXT, result);
        // 如果找到组链方法，报错
        if (method == null)
            throw new IllegalStateException("No such chain method " + NEXT
                    + "(next) in class " + element.getClass().getCanonicalName());

        // 调用next方法 来 组链，通过 next方法将 result(即上一个SPI实例）设置到SPI实例中
        invokeMethod(element, method, result);

        // 返回当前SPI实例
        return element;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T[]> getArrayTypeOf(Class<T> componentType) {
        return (Class<T[]>) Array.newInstance(componentType, 0).getClass();
    }

}
