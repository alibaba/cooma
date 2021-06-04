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
import com.alibaba.microkernel.convention.Initializable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * 实例化约定方法初始化微内核基类(微内核责任链"后置"拦截)。
 * <p/>
 * 内置方法初始化约定如：
 * <ul>
 *     <li>{@link IocMicrokernel}: {@code set*(value)}</li>
 *     <li>{@link IocMicrokernel}/{@link Initializable}: {@code boolean init()}</li>
 *     <li>{@link AopMicrokernel}: {@code setNext(interface)}</li>
 * </ul>
 *
 * @author Liang Fei
 * @see IocMicrokernel
 * @see Initializable
 * @see AopMicrokernel
 */
public abstract class AbstractMethodMicrokernel extends MicrokernelChain {

    protected static Method findMethod(Object object, String methodName, Object... args) {
        Method[] methods = object.getClass().getMethods();
        return Stream.of(methods)
                .filter(m -> m.getName().equals(methodName)
                        && m.getParameterCount() == args.length
                        && Modifier.isPublic(m.getModifiers()))
                .findFirst()
                .orElse(null);
    }

    protected static Object invokeMethod(Object object, String methodName, Object... args) {
        return invokeMethod(object, findMethod(object, methodName, args), args);
    }

    protected static Object invokeMethod(Object object, Method method, Object... args) {
        try {
            if (object != null && method != null)
                return method.invoke(object, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            Throwable t = e instanceof InvocationTargetException ? e.getCause() : e;
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new IllegalStateException("Failed to invoke method "
                    + object.getClass().getCanonicalName()
                    + "." + method.getName()
                    + "(" + Arrays.toString(method.getParameterTypes()) + ")"
                    + " by args: " + Arrays.toString(args) + ", cause: "
                    + e.getClass().getCanonicalName() + ": " + t.getMessage(), t);
        }
        return null;
    }

}
