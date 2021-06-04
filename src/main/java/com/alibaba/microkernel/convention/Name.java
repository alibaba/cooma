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

import com.alibaba.microkernel.convention.internal.SpiMicrokernel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPI名称
 * <p/>
 * 注：SpiMicrokernel并不强依赖此注解，默认使用类名，或接口名前缀作为对象名。<br>
 * 如：{@code com.xxx.TraceFilter} 的默认名为：{@code trace}
 *
 * @author Liang Fei
 * @see SpiMicrokernel
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Name {

    /**
     * 对象名称
     *
     * @return 名称
     */
    String value();

}
