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

package com.alibaba.cooma.ext6.impl;

import com.alibaba.cooma.ext1.SimpleExt;
import com.alibaba.cooma.ext6.Dao;
import com.alibaba.cooma.ext6.InjectExt;
import junit.framework.Assert;

import java.util.Map;

/**
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 */
public class Ext6Impl1 implements InjectExt {
    SimpleExt simpleExt;
    public Dao obj;

    public void setDao(Dao obj) {
        this.obj = obj;

        Assert.assertNotNull("inject extension instance can not be null", obj);
        Assert.fail();
    }

    public void setSimpleExt(SimpleExt simpleExt) {
        this.simpleExt = simpleExt;
    }

    public String echo(Map<String, String> config, String s) {
        return "Ext6Impl1-echo-" + simpleExt.echo(config, s);
    }
}
