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

package com.alibaba.demo.cooma;

import com.alibaba.cooma.ExtensionLoader;
import com.alibaba.demo.cooma.car.Car;
import com.alibaba.util.Utils;

import java.util.Arrays;

/**
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 */
public class CarDemo {
    public static void main(String[] args) {
        ExtensionLoader<Car> extensionLoader = ExtensionLoader.getExtensionLoader(Car.class);

        // 演示 扩展的获得、关联扩展点的注入（Car扩展点引用了Wheel）

        Car racingCar = extensionLoader.getExtension("racing");
        racingCar.run(Utils.kv2Map("wheel", "wood")); // 通过Key指定要哪种轮子

        System.out.println("=================================");

        Car sportCar = extensionLoader.getExtension("sport");
        sportCar.run(Utils.kv2Map("k1", "v1")); // 缺省使用RubberWheel

        // 演示 Adaptive Instance的使用

        System.out.println("=================================");

        Car adaptiveInstance = extensionLoader.getAdaptiveInstance();
        adaptiveInstance.run(Utils.kv2Map("car", "racing")); // 通过car key指定的Car本身使用哪个实现。

        // 演示 Wrapper的使用

        System.out.println("=================================");

        Car countedSportCar = extensionLoader.getExtension("sport", Arrays.asList("run_counter"));
        countedSportCar.run(Utils.kv2Map("k1", "v1")); // 缺省使用RubberWheel

        // 演示 在Adaptive Instance上Wrapper的使用

        System.out.println("=================================");

        Car countedAdaptiveInstance = extensionLoader.getAdaptiveInstance(Arrays.asList("run_counter"));
        countedAdaptiveInstance.run(Utils.kv2Map("car", "racing")); // 通过car key指定的Car本身使用哪个实现。
    }
}
