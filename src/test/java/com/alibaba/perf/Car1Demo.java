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

package com.alibaba.perf;

import com.alibaba.cooma.ExtensionLoader;
import com.alibaba.util.Utils;

/**
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 */
public class Car1Demo {
    public static void main(String[] args) {
        ExtensionLoader<Car1> extensionLoader = ExtensionLoader.getExtensionLoader(Car1.class);

        // warm-up
        long tick = System.currentTimeMillis();
        for (int i = 0; i < 10 * 1000 * 1000; ++i) {
            Car1 adaptiveInstance = extensionLoader.getAdaptiveInstance();
            adaptiveInstance.run(Utils.kv2Map("object", "racing")); // 通过car key指定的Car本身使用哪个实现。
        }
        System.out.println(System.currentTimeMillis() - tick);

        // 1-st key
        tick = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            Car1 adaptiveInstance = extensionLoader.getAdaptiveInstance();
            adaptiveInstance.run(Utils.kv2Map("car", "racing")); // 通过car key指定的Car本身使用哪个实现。
        }
        System.out.println(System.currentTimeMillis() - tick);

        // 4-th key
        tick = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            Car1 adaptiveInstance = extensionLoader.getAdaptiveInstance();
            adaptiveInstance.run(Utils.kv2Map("machine", "racing")); // 通过car key指定的Car本身使用哪个实现。
        }
        System.out.println(System.currentTimeMillis() - tick);

        // 4-th key
        tick = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            Car1 adaptiveInstance = extensionLoader.getAdaptiveInstance();
            adaptiveInstance.run(Utils.kv2Map("machine", "racing")); // 通过car key指定的Car本身使用哪个实现。
        }
        System.out.println(System.currentTimeMillis() - tick);

        // 7-th key
        tick = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            Car1 adaptiveInstance = extensionLoader.getAdaptiveInstance();
            adaptiveInstance.run(Utils.kv2Map("object", "racing")); // 通过car key指定的Car本身使用哪个实现。
        }
        System.out.println(System.currentTimeMillis() - tick);
    }
}
