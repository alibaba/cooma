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

package com.metaframe.cooma.ext3.impl;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ext3.WrappedExt;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 */
public class Ext3Wrapper2 implements WrappedExt {
    WrappedExt instance;

    public static AtomicInteger echoCount = new AtomicInteger();
    public static AtomicInteger yellCount = new AtomicInteger();

    public Ext3Wrapper2(WrappedExt instance) {
        this.instance = instance;
    }

    public String echo(Config config, String s) {
        echoCount.incrementAndGet();
        return instance.echo(config, s);
    }

    public String yell(Config config, String s) {
        yellCount.incrementAndGet();
        return instance.yell(config, s);
    }
}