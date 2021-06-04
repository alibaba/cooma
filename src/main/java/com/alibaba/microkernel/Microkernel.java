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

import com.alibaba.microkernel.convention.Closeable;
import com.alibaba.microkernel.convention.internal.BootstrapMicrokernel;

/**
 * 微内核容器. (Static, Singleton, ThreadSafe)
 * <p/>
 * 该PluginFactory是一个轻量级“IoC+AOP”容器。
 * <p/>
 * 使用方式如：
 * <pre>{@code Foo foo = Microkernel.getMicrokernel().create(Foo.class);}</pre>
 * <p/>
 * IoC: 基于Setter递归注入属性，如：
 * <pre>{@code
 * public class Example {
 *
 *     // 扩展点必需有无参构造函数，否则无法实例化
 *     // 对象会被单例缓存
 *     public Example() {}
 *
 *     // 配置：foo.bar=abc
 *     // 将点号转为大写驼峰命名注入
 *     public void setFooBar(String fooBar) {
 *         this.fooBar = fooBar;
 *     }
 *
 *     // 配置：foo=foo1
 *     // 将从 META-INF/services/com.xxx.Foo 文件中查找类名为Foo1或Foo1Foo的插件
 *     // 其中，Foo对象的Setter同样会被递归注入属性
 *     public void setFoo(Foo foo) {
 *         this.foo = foo;
 *     }
 *
 *     // 配置：foos=foo1,foo2
 *     // 可以注入数组，可以是基本类型String[]或Bean对象Foo[]，多个值用逗号分隔。
 *     // 也可以使用加减号修改列表配置：(参见：Configurer)
 *     // foos+=foo3
 *     // foos-=foo2
 *     public void setFoos(Foo[] foos) {
 *         this.foos = foos;
 *     }
 *
 *     // 所有属性注入完成后，会执行init()方法。
 *     public void init() {
 *         // do something...
 *     }
 *
 *     // init方法可以有boolean返回值，如果返回false，当前对象会被失效删除。
 *     // 可以用于如果某字段未配置，自动不加载此扩展，比如：监控端口未配置，则MonitorFilter不加载。
 *     public boolean init() {
 *         return fooBar != null;
 *     }
 *
 * } }
 * </pre>
 * <p/>
 * AOP: 基于next()组装责任链，如：
 * <pre> {@code
 * public class WrapperFoo implements Foo {
 *
 *     private Foo foo;
 *
 *     // 配置：foo=foo1,foo2,foo3
 *     // 会把后一个对象next到前一个对象的属性中
 *     // 即：foo1.setNext(foo2); foo2.setNext(foo3); return foo1;
 *     // 也可以使用加减号配置：(参见：Configurer)
 *     // foo=foo1,foo2
 *     // foo+=foo3
 *     // foo^=foo0
 *     // foo-=foo2
 *     public void setNext(Foo setNext) {
 *         this.setNext = setNext;
 *     }
 *
 *  } }</pre>
 *
 * @author Liang Fei
 */
public interface Microkernel extends Closeable {

    /**
     * 获取微内核容器，该微内核会用默认引导容器加载用户自定义容器。
     *
     * @return 微内核容器.
     */
    static Microkernel getMicrokernel() {
        return BootstrapMicrokernel.getMicrokernel();
    }

    /**
     * 创建对象。
     *
     * @param type   对象类型
     * @param values 对象的值
     * @param <T>    对象类型
     * @return 对象实例
     */
    <T> T create(Class<T> type, String... values);

}
