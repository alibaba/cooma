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
import com.alibaba.microkernel.convention.Initializable;
import com.alibaba.microkernel.convention.Order;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * 基于{@code Java}的{@code Bean}规范的{@code Setter}方法注入依赖微内核(微内核责任链"后置"拦截)。
 * <p/>
 * <ul>
 *      <li>支持递归注入，即如果注入的为对象，则被注入的对象的Setter方法会被递归注入</li>
 *      <li>被注入过的对象会被单例缓存，不会重复注入</li>
 *      <li>支持循环引用，即支持A引用B，B又引用A的情况</li>
 *      <li>优先查找以当前对象配置值为前缀的配置，比如：{@code protocol=http}，则HttpProtocol的属性注入 {@code http.timeout=xxx} 优于 {@code timeout=xxx}</li>
 * </ul>
 * <p/>
 * 如，配置：
 * <pre>{@code
 * protocol=http
 * timeout=1000
 * }</pre>
 * 注入代码：
 * <pre>{@code
 * public class HttpProtocol implements Protocol {
 *
 *     private int timeout;
 *
 *     // 将被注入 http.timeout=1000 或 timeout=1000 配置的值
 *     // http.timeout优先于timeout，以避免同名配置冲突
 *     public void setTimeout(int timeout) {
 *         this.timeout = timeout;
 *     }
 * }
 * }</pre>
 * <p/>
 * init默认配置在setter微内核之后，即：init()方法被调用时，setter的属性已被注入完成
 * <p/>
 * 如：
 * <pre>{@code
 * public class TraceFilter implements Filter {
 *
 *     private String traceLogPath;
 *     public void setTraceLogPath(String traceLogPath) {
 *         this.traceLogPath = traceLogPath;
 *     }
 *
 *     // 如果traceLogPath没有配置，则当前TraceFilter不会被加载
 *     // 就算filters+=trace被配置上了也会跳过，用于减少冗余对象的加载
 *     public boolean init() {
 *         return traceLogPath != null;
 *     }
 * }
 * }</pre>
 *
 * @author Liang Fei
 */
@Order(230)
public class IocMicrokernel extends AbstractMethodMicrokernel implements Initializable {

    private static final String SET = "set";

    private static final String INIT = "init";

    private static final Object NULL = new Object();

    private final ConcurrentMap<String, Object> singletons = new ConcurrentHashMap<>();

    private Microkernel microkernel;

    /**
     * 设置根节点微内核，使setter的注入对象能完整初始化
     *
     * @param microkernel 根节点微内核
     * @return 当前微内核自身，用于链式设置调用
     */
    public IocMicrokernel setMicrokernel(Microkernel microkernel) {
        this.microkernel = microkernel;
        return this;
    }

    /**
     * 初始化
     *
     * @return 是否初始化成功
     */
    @Override
    public boolean init() {
        return microkernel != null;
    }

    @Override
    public <T> T create(Class<T> type, String... values) {
        // IOC 只处理 接口类型 且 只有一个值 的情况
        if (!type.isInterface() || values.length != 1) {
            return super.create(type, values);
        }

        // final String value = getCanonicalSpiName(type, values[0]); // 唯一的值，即扩展名
        // TODO 先简单统一成小写的，避免因为Key的大小写不同，singletons 重复缓存 一个SPI的实例
        //      后面 要统一梳理 Key的约定
        final String value = values[0].toLowerCase();

        // 加入缓存，处理循环引用
        String key = type.getCanonicalName() + "=" + value;
        Object singleton = singletons.get(key);
        if (singleton != null)
            return singleton == NULL ? null : (T) singleton;

        // 先调用责任链的下一个微内核创建好实例
        T object = super.create(type, value);

        // 加入缓存，处理循环引用
        singletons.put(key, object == null ? NULL : object);
        // 后置拦截，递归注入所有setter方法的值
        if (object == null) {
            return null;
        }

        if (microkernel != null) {
            // 获取插件所有setter方法，并注入值
            Method[] methods = object.getClass().getMethods();
            Stream.of(methods)
                    // 找到所有setter方法
                    .filter(IocMicrokernel::isSetterMethod)
                    // 将解析出来的对象实例，注入到setter方法中
                    .forEach(method -> doSet(object, method, value));
        }

        // 在所有属性被setter后，执行对象的init()方法，用于对象的自我初始化检查
        Object result = invokeMethod(object, INIT);
        if (Boolean.FALSE.equals(result)) {
            // 如果返回false，则放弃该对象，用于减少冗余插件加载
            singletons.put(key, NULL);
            return null;
        }

        return object;
    }

    /**
     * 规范化扩展名：
     * <ol>
     * <li>com.xxx.FooInterfaceName -> foo，接口名是 com.yyy.InterfaceName</li>
     * <li>全类名</li>
     * </ol>
     */
    private static <T> String getCanonicalSpiName(Class<T> type, String value) {
        int lastIndex = value.lastIndexOf(".");
        if (lastIndex > 0) {
            value = value.substring(lastIndex); // base name
        }

        String typeName = type.getSimpleName();
        lastIndex = typeName.lastIndexOf(".");
        if (lastIndex > 0) {
            typeName = typeName.substring(lastIndex); // type base name
        }


        if (value.endsWith(typeName) && value.length() > typeName.length()) {
            value = value.substring(0, typeName.length());
        }

        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private <T> void doSet(T object, Method method, String value) {
        String name = method.getName();
        // set style
        if (name.startsWith(SET)) {
            name = name.substring(SET.length());
        }
        String upper = name.substring(0, 1).toUpperCase() + name.substring(1);

        // 获取带前缀的属性名
        String[] names = Stream.concat(
                Stream.of(value).map(v -> v + upper),
                Stream.of(name)
        ).toArray(String[]::new);

        final Class<?> onlyParameterType = method.getParameterTypes()[0];
        Object arg = microkernel.create(onlyParameterType, names);
        if (arg != null) {
            invokeMethod(object, method, arg);
        }
    }

    private static boolean isSetterMethod(Method method) {
        // set style
        final boolean startWithSet = method.getName().startsWith(SET);
        // builder style
        final boolean returnTypeIsSelf = method.getReturnType().equals(method.getDeclaringClass());
        return (startWithSet || returnTypeIsSelf)
                && Modifier.isPublic(method.getModifiers()) // is public
                && !Modifier.isStatic(method.getModifiers()) // not static
                && method.getParameterTypes().length == 1; // only one parameter
    }

    @Override
    public void close() {
        singletons.clear();
        super.close();
    }

}
