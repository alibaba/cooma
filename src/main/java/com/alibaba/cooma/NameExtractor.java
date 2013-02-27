/*
 * Copyright 2012-2013 Cooma Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cooma;

/**
 * 从方法扩展点的方法参数中提取到扩展名称信息。
 */
public interface NameExtractor {
    /**
     * @param type 方法参数类型。
     */
    void setParameterType(Class<?> type);

    /**
     * @param adaptive 方法参数的{link Adaptive}注解。
     */
    void setAdaptive(Adaptive adaptive);

    /**
     * After Properties set.
     */
    void init();

    /**
     * 从方法扩展点的方法参数中提取到扩展名称信息。
     *
     * @param argument 方法参数。
     * @return 返回提取到的扩展名称。<code>null</code>表示提取到的信息为空。
     */
    Object getValue(Object argument);
}
