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

import com.alibaba.microkernel.convention.internal.CloseableMicrokernel;

/**
 * 可关闭对象接口
 * <p>
 * 注：CloseableMicrokernel并不依赖此接口，只要组链类有close()方法，就会被反射调用
 *
 * @author Liang Fei
 * @see CloseableMicrokernel
 */
public interface Closeable extends AutoCloseable {

    /**
     * 关闭对象，释放其资源
     * <p>
     * <p/> 注：倒序关闭，被依赖者先于依赖者关闭
     */
    void close();

}
