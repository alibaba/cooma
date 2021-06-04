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

import com.alibaba.microkernel.convention.internal.AopMicrokernel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自我排序。扩展者之间进行order区间约定，减少用户使用时对扩展点顺序的理解成本。
 * <p>
 * 注：ChainMicrokernel并不依赖此注解，只要组链类有{@code int order()}方法，就会被反射调用，并用于排序。
 *
 * @author Liang Fei
 * @see AopMicrokernel
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {

    /**
     * 排序值，数字越小越在前面
     *
     * @return 排序值
     */
    int value();

}
