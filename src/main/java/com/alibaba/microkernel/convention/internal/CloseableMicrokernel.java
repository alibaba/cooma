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

import com.alibaba.microkernel.Microkernel;
import com.alibaba.microkernel.convention.Order;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 关闭和释放对象微内核（微内核责任链"后置"拦截）。
 * <p>
 * 关闭由该微内核创建的所有实现AutoCloseable的对象。
 * 如果某个对象关闭出现异常，不影响其它对象关闭，并汇总所有异常记录在最后抛出异常。
 *
 * @author Liang Fei
 */
@Order(220)
public class CloseableMicrokernel extends AbstractMethodMicrokernel {

    private static final String CLOSE = "close";

    private final List<Object> closeableList = new CopyOnWriteArrayList<>();

    @Override
    public <T> T create(Class<T> type, String... values) {
        T object = super.create(type, values);
        if (object != null
                && !(object instanceof Microkernel)
                && !closeableList.contains(object)
                && findMethod(object, CLOSE) != null) {
            // 插在头部，倒序。在关闭时，被依赖者先于依赖者关闭
            closeableList.add(0, object);
        }
        return object;
    }

    @Override
    public void close() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        for (Object closeable : closeableList) {
            try {
                invokeMethod(closeable, CLOSE, new Object[0]);
            } catch (Throwable e) {
                e.printStackTrace(printWriter);
            }
        }
        closeableList.clear();

        try {
            super.close();
        } catch (Throwable e) {
            e.printStackTrace(printWriter);
        }

        String string = stringWriter.toString();
        if (string.length() > 0) {
            throw new IllegalStateException(string);
        }
    }

}
