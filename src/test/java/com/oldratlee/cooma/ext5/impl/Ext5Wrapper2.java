/*
 * Copyright 1999-2011 Alibaba Group.
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
package com.oldratlee.cooma.ext5.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.oldratlee.cooma.Configs;
import com.oldratlee.cooma.ext5.Ext5NoAdaptiveMethod;

/**
 * @author oldratlee
 */
public class Ext5Wrapper2 implements Ext5NoAdaptiveMethod {
    Ext5NoAdaptiveMethod instance;
    
    public static AtomicInteger echoCount = new AtomicInteger();
    public static AtomicInteger yellCount = new AtomicInteger();
    public static AtomicInteger bangCount = new AtomicInteger();
    
    public Ext5Wrapper2(Ext5NoAdaptiveMethod instance) {
        this.instance = instance;
    }
    
    public String echo(Configs config, String s) {
        echoCount.incrementAndGet();
        return instance.echo(config, s);
    }

    public String yell(Configs config, String s) {
        yellCount.incrementAndGet();
        return instance.yell(config, s);
    }

    public String bang(Configs config, int i) {
        bangCount.incrementAndGet();
        return instance.bang(config, i);
    }
}