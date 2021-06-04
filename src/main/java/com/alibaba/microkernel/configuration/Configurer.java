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
package com.alibaba.microkernel.configuration;

import java.util.stream.Stream;

/**
 * 配置器.
 * <p/>
 * 使用方式如：
 * <pre>{@code Configurer configurer = Microkernel.getMicrokernel().create(Configurer.class);}</pre>
 * <p/>
 * 支持配置增减：
 * <ul>
 *     <li>"+=" 加号：表示在多个配置值的最后，追加配置，用于增加自己的扩展实现，如：
 *         <ul>
 *             <li>追加单个值：{@code filters+=myfilter}</li>
 *             <li>追加多个值，用逗号分隔：{@code filters+=myfilter1,myfilter2}</li>
 *             <li>追加在某个值后，被追加位置值用冒号分隔：{@code filters+=trace:myfilter1,classloader:myfilter2}</li>
 *         </ul>
 *     </li>
 *     <li>"^=" 插入：表示在多个配置值的最前，插入配置，功能同加号，只是加入位置不同，如：
 *         <ul>
 *             <li>插入单个值：{@code filters^=myfilter}</li>
 *             <li>插入多个值，用逗号分隔：{@code filters^=myfilter1,myfilter2}</li>
 *             <li>插入在某个值前，被插入位置值用冒号分隔：{@code filters^=trace:myfilter1,classloader:myfilter2}</li>
 *         </ul>
 *     <li>"~=" 替换：表示替换多个配置值中的某个值，被替换值和新值用冒号分隔，如：
 *         <ul>
 *             <li>替换单个值：{@code filters~=trace:mytrace}</li>
 *             <li>替换多个值，用逗号分隔：{@code filters~=trace:mytrace,classloader:myclassloader}</li>
 *         </ul>
 *     </li>
 *     <li>"-=" 减号：表示在移除多个配置值中的某个值，用于移除框架某个内置实现，如：
 *         <ul>
 *             <li>移除单个值：{@code filters-=trace}</li>
 *             <li>移除多个值，用逗号分隔：{@code filters-=trace,classloader}</li>
 *         </ul>
 *     </li>
 *     <li>"=" 覆盖：表示覆盖原有内置所有配置，只使用当前配置，如：
 *         <ul>
 *             <li>多个值用逗号分隔：{@code filters=myfilter1,myfilter2}</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author Liang Fei
 */
public interface Configurer {

    /**
     * 基于配置名查找配置，多个值以流的形式返回.
     *
     * @param key 配置名
     * @return 配置值
     */
    Stream<String> get(String key);

}
