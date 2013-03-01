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

package com.alibaba.cooma.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 和{@link MapSourceExtractor}配合使用，添加此注解，
 *
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 * @see MapSourceExtractor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface FromAttribute {
    /**
     * 指定属性名，表示从所注解方法参数的这个属性上提取扩展实现名。
     *
     * @see com.alibaba.cooma.NameExtractor
     * @see MapSourceExtractor
     */
    String value();
}
