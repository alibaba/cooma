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
import com.alibaba.microkernel.convention.Order;

import java.net.URI;

/**
 * 原始值类型创建微内核. (微内核责任链"前置"拦截)
 * <p/>
 * 支持类型：boolean/Boolean, short/Short, int/Integer, long/Long, float/Float, double/Double, char/Character, String
 * <p/>
 * 如果类型为原始类型，则直接解析值返回，如：
 * <pre>{@code
 * primitiveMicrokernel.create(Integer.class, "123");
 * }</pre>
 * 则返回结果为: 123
 *
 * @author Liang Fei
 */
@Order(200)
public class PrimitiveMicrokernel extends MicrokernelChain {

    /**
     * 解析原始类型的值
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> type, String... values) {
        if (values.length <= 0) {
            return super.create(type, values);
        }

        // 对String/URI 直接支持
        if (type == String.class) {
            return (T) String.join("", values);
        } else if (type == URI.class) {
            return (T) URI.create(String.join("", values));
        }

        //  对原始类型只支持一个值。
        //  FIXME 如果有多是不是这里更严格的报错出来？能有更明确的出错信息
        if (values.length != 1) {
            return super.create(type, values);
        }

        final String value = values[0];
        if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(value);
        } else if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(value);
        } else if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(value);
        } else if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(value);
        } else if (type == short.class || type == Short.class) {
            return (T) Short.valueOf(value);
        } else if (type == byte.class || type == Byte.class) {
            return (T) Byte.valueOf(value);
        } else if ((type == char.class || type == Character.class)
                && value.length() == 1) {
            return (T) Character.valueOf(value.charAt(0)); // FIXME 只取一个 Char。有多 是不是 更严格的报错？
        }

        // 非原始类型，交给下一个微内核创建
        return super.create(type, values);
    }
}
