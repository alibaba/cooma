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

import com.alibaba.microkernel.convention.Order;

/**
 * 系统环境变量配置器(配置器责任链"后置"拦截)。
 * <p/>
 * 可用于系统运维时，替换代码中的配置，优先级高于命令行及配置文件。配置如：
 * <pre>{@code
 * export xxx=xxx
 * export default.timeout=1000
 * }</pre>
 *
 * @author Liang Fei
 */
@Order(200)
public class EnvironmentConfigurer extends SystemConfigurer {

    @Override
    protected String getProperty(String key) {
        // 读取环境变量
        return System.getenv(key);
    }

}
