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
import com.alibaba.microkernel.configuration.Configurer;
import com.alibaba.microkernel.configuration.internal.*;

/**
 * 默认引导初始化微内核
 *
 * @author Liang Fei
 */
public class BootstrapMicrokernel {

    public static Microkernel getMicrokernel() {
        // 硬编码组装配置器
        Configurer configurer = new DotConfigurer()
                .setNext(new RemoveConfigurer()
                .setNext(new ReplaceConfigurer()
                .setNext(new PrependConfigurer()
                .setNext(new AppendConfigurer()
                .setNext(new ReferenceConfigurer()
                .setNext(new ListConfigurer()
                .setNext(new OverrideConfigurer()
                .setNext(new EnvironmentConfigurer()
                .setNext(new SystemConfigurer()
                .setNext(new PropertiesConfigurer()))))))))));

        // 硬编码组装微内核
        ConfigMicrokernel microkernel = new ConfigMicrokernel();
        microkernel.setConfigurer(configurer)
                .setNext(new AllMicrokernel()
                .setNext(new AopMicrokernel()
                .setNext(new ArrayMicrokernel()
                .setNext(new PrimitiveMicrokernel()
                .setNext(new CloseableMicrokernel()
                .setNext(new IocMicrokernel()
                        .setMicrokernel(microkernel) // 递归引用
                .setNext(new SpiMicrokernel())))))));

        // 加载用户自定义配置器，替换硬编码配置器
        // ConfigMicrokernel会自我更新Configurer
        // 比如用户可配置：configurer+=xxx 或 configurer-=system
        microkernel.create(Configurer.class);

        // 加载用户自定义微内核，替换硬编码微内核
        // 比如用户可配置：microkernel+=xxx 或 microkernel-=all
        return microkernel.create(Microkernel.class);
    }

}
