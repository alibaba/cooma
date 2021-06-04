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
package com.alibaba.microkernel;

import com.alibaba.microkernel.convention.Chain;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 可组成责任链的微内核基类
 *
 * @author Liang Fei
 */
public abstract class MicrokernelChain implements Microkernel, Chain<Microkernel> {

    /**
     * 责任链下一个微内核
     */
    private Microkernel next;

    /**
     * 注入责任链下一个微内核
     *
     * @param next 注入下一个微内核
     * @return 当前微内核自身，用于链式设置调用
     */
    @Override
    public MicrokernelChain setNext(Microkernel next) {
        this.next = next;
        return this;
    }

    /**
     * 如果责任链的下一个微内核不为空，则执行，否则返回空
     *
     * <pre>{@code
     * // 执行责任链的下一个微内核
     * return super.create(type, values);
     * }</pre>
     *
     * @param type   对象类型
     * @param values 对象的值
     * @param <T>    对象类型
     * @return 对象的实例
     */
    public <T> T create(Class<T> type, String... values) {
        Objects.requireNonNull(type, "The created type can not be null.");
        Objects.requireNonNull(values, "The created values can not be null.");
        if (next == null) {
            if (values.length > 0) // 如果有配置值，但却没有被处理，报错提醒用户配置错误
                throw new IllegalArgumentException("Could not be created \"" + type.getCanonicalName()
                        + "\" by values \"" + Stream.of(values).collect(Collectors.joining(",")) + "\"");
            return null;
        }
        return next.create(type, values);
    }

    public void close() {
        if (next != null) {
            next.close();
        }
    }

}
