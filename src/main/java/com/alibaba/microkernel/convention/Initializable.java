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
package com.alibaba.microkernel.convention;

import com.alibaba.microkernel.convention.internal.IocMicrokernel;

/**
 * 可初始化对象接口
 * <p>
 * 注：SetterMicrokernel并不依赖此接口，只要组链类有init()方法，就会被反射调用
 *
 * @author Liang Fei
 * @see IocMicrokernel
 */
public interface Initializable {

    /**
     * 初始化对象
     *
     * <pre>{@code
     * public class TraceFilter implements Filter {
     *     public boolean init() {
     *         // 如果跟踪日志文件没有配置，则跟踪拦截器也不加载
     *         return traceLogPath != null;
     *     }
     * }
     * }</pre>
     *
     * @return 是否初始化成功，不成功的对象将被放弃加载，减少冗余类
     */
    boolean init();

}
