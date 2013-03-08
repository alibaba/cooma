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

package com.alibaba.demo.cooma.car.impl;

import com.alibaba.demo.cooma.car.Car;
import com.alibaba.demo.cooma.wheel.Wheel;

/**
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 */
public class SportCar implements Car {

    private Wheel wheel;

    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
    }

    public void run() {
        wheel.roll();
        System.out.println("SportCar Running...");
    }
}
