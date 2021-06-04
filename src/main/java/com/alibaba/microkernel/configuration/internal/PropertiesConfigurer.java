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
import com.alibaba.microkernel.convention.Closeable;
import com.alibaba.microkernel.convention.Initializable;
import com.alibaba.microkernel.convention.Order;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Properties文件配置器(配置器责任链"后置"拦截)。
 * <p/>
 * 配置如：
 * <p>
 * META-INF/microkernel.properties
 * <pre>{@code
 * default.timeout=1000
 * }</pre>
 *
 * @author Liang Fei
 */
@Order(300)
public class PropertiesConfigurer extends ConfigurerChain implements Initializable, Closeable {

    private static final String DEFAULT_PROPERTIES = "META-INF/microkernel.properties";

    private static final String PROPERTIES_URLS = "properties.urls+";

    private static final String PROPERTIES_ORDER = "properties.order";

    private static final int DEFAULT_ORDER = 100;

    private static final String PROFILE_SPLIT = "-";

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+$");

    private final Set<Properties> propertiesSet = new ConcurrentSkipListSet<>(
            Comparator.comparingInt((Properties p) -> Integer.parseInt(p.getProperty(PROPERTIES_ORDER)))
                    .thenComparing((Properties p) -> p.getProperty(PROPERTIES_URLS)));

    private String[] paths;

    private String[] profiles;

    public PropertiesConfigurer() {
        // 加载默认配置
        load(DEFAULT_PROPERTIES, DEFAULT_ORDER);
    }

    /**
     * 设置配置加载路径
     *
     * @param paths 配置加载路径
     * @return 当前配置器本身
     */
    public PropertiesConfigurer setPaths(String[] paths) {
        this.paths = paths;
        if (paths != null && paths.length > 0) {
            // 用自增保证用户配置顺序
            AtomicInteger i = new AtomicInteger();
            // 加载配置文件
            Stream.of(paths).forEach(path -> load(path, DEFAULT_ORDER * 10 + i.getAndIncrement()));
        }
        return this;
    }

    /**
     * 设置环境配置，如：
     * <ul>
     *     <li>配置Path为：{@code META-INF/my.properties}</li>
     *     <li>配置Profile为：{@code debug}</li>
     *     <li>则加载配置文件为：{@code META-INF/my.properties, META-INF/my-debug.properties}</li>
     * </ul>
     *
     * @param profiles 环境配置后缀
     * @return 当前配置器本身
     */
    public PropertiesConfigurer setProfiles(String[] profiles) {
        this.profiles = profiles;
        return this;
    }

    /**
     * 初始化配置器
     */
    @Override
    public boolean init() {
        // profiles依赖paths，而setter不确保顺序，放到init()方法中执行拼接配置名加载
        if (paths != null && paths.length > 0) {
            // TODO 不应特殊处理
            if (profiles == null || profiles.length == 0)
                profiles = get("properties.profiles").toArray(String[]::new);
            if (profiles != null && profiles.length > 0) {
                // 用自增保证用户配置顺序
                AtomicInteger i = new AtomicInteger();
                Stream.of(paths)
                        // 分隔path的前后缀
                        .map(path -> new String[]{path.substring(0, path.lastIndexOf('.')),
                                path.substring(path.lastIndexOf('.'))})
                        // 为每个path添加所有profile后缀
                        .flatMap(entry -> Stream.of(profiles)
                                .map(profile -> entry[0] + PROFILE_SPLIT + profile + entry[1]))
                        // 加载profile配置
                        .forEach(path -> load(path, DEFAULT_ORDER * 100 + i.getAndIncrement()));
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public Stream<String> get(@Nonnull String key) {
        // 展开所有properties文件的同key值
        Stream<String> stream = propertiesSet.stream()
                .map(p -> p.getProperty(key))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> v.length() > 0);
        return Stream.concat(super.get(key), stream);
    }

    // 加载指定路径的配置，并以order进行排序
    private void load(String path, int order) {
        try {
            final Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(path);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                Properties properties = new Properties();
                // 加载properties配置
                properties.load(url.openStream());
                // 向properties中加入properties.url值，用于调试时显示配置位置
                // 使用者也可以通过setPropertiesUrls(String[]){}获取到所有已加载的配置URL，可用于检查配置冲突等
                properties.setProperty(PROPERTIES_URLS, url.toString());
                // 向properties中加入properties.order值，用于properties排序
                // 如果使用者已主动在properties文件中加入properties.order值，则使用其配置，否则使用默认计算顺序值
                String value = properties.getProperty(PROPERTIES_ORDER);
                if (value == null || value.length() == 0 || !NUMBER_PATTERN.matcher(value).matches()) {
                    properties.setProperty(PROPERTIES_ORDER, String.valueOf(order));
                }
                // 加入到properties集合中
                propertiesSet.add(properties);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration in classpath "
                    + path + ", cause: " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        propertiesSet.clear();
    }

}
