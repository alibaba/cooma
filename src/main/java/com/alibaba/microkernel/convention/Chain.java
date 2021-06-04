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

/**
 * 可组装责任链接口
 * <p>
 * 注：ChainMicrokernel并不依赖此接口，只要组链类有setNext(T)方法，就会被反射注入
 *
 * @author Liang Fei
 * @see AopMicrokernel
 */
public interface Chain<T> {

    /**
     * 组装责任链，设置下一个责任链实现
     *
     * @param next 下一个实现
     * @return 当前链
     */
    Chain<T> setNext(T next);

}
