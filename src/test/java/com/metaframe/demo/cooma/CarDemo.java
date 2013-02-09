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

package com.metaframe.demo.cooma;

import com.metaframe.cooma.Config;
import com.metaframe.cooma.ExtensionLoader;
import com.metaframe.demo.cooma.car.Car;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class CarDemo {
    public static void main(String[] args) {
        ExtensionLoader<Car> extensionLoader = ExtensionLoader.getExtensionLoader(Car.class);

        Car racingCar = extensionLoader.getExtension("racing");
        racingCar.run(Config.fromKv("wheel", "wood")); // 通过Key指定要哪种轮子

        System.out.println("=================================");

        Car sportCar = extensionLoader.getExtension("sport");
        sportCar.run(Config.fromKv("k1", "v1")); // 缺省使用RubberWheel
    }
}
