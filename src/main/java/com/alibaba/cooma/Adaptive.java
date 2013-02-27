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

package com.alibaba.cooma;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * 注解到扩展点方法的参数，表示这个参数用于提供信息，让自适应实例（Adaptive Instance）找到运行调用时要调用的扩展名称。
 *
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 * @see ExtensionLoader
 * @see Extension
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Adaptive {
    /**
     * Key列表，这些Key在所注解参数的对应的Value是自适应实例方法调用时真正执行调用的扩展的名称。
     * <br/>
     * 如果这些Key在中所注解参数都没有Value，自适应实例会使用缺省扩展（在扩展点接口的{@link Extension}上设置的）。
     * <p/>
     * eg. <code>String[] {"key1", "key2"}</code>表示：
     * <ol>
     * <li>使用<code>key1</code>的值作为自适应实例真正调用的扩展实现的名称。
     * <li>如果<code>key1</code>没有对应的值，则使用<code>key2</code>的值作为自适应实例真正调用的扩展实现的名称。
     * <li>如果<code>key2</code>没有对应的值，则使用缺省扩展。
     * <li>如果没有缺省扩展，则在获取自适应实例时，会抛出{@link IllegalStateException}。
     * </ol>
     * <p/>
     * 缺省是扩展点接口名的点分小写形式。
     * eg. 扩展点接口名<code>com.alibaba.cooma.FooBizService</code>, 缺省key是<code>String[] {"foo.biz.service"}</code>。
     *
     * @see Extension#value()
     * @since 0.1.0
     */
    String[] value() default {};

    String path() default "";

    Class<? extends NameExtractor> extractor() default DefaultNameExtractor.class;

    /**
     * 从方法扩展点的方法参数中提取到扩展名称信息。
     */
    public static interface NameExtractor {
        /**
         * 从方法扩展点的方法参数中提取到扩展名称信息。
         *
         * @param type 方法参数类型。
         * @param argument 方法参数。
         * @param adaptive 方法参数的{link Adaptive}注解。
         * @return 返回提取到的扩展名称。<code>null</code>表示提取到的信息为空。
         */
        Object getValue(Class<?> type, Object argument, Adaptive adaptive);
    }

    public static class DefaultNameExtractor implements NameExtractor {
        public String getValue(Class<?> type, Object argument, Adaptive adaptive) {
            // 方法参数类型是String，参数值直接作为扩展名称。
            if (type == String.class) return (String) argument;

            final String[] keys = adaptive.value();

            // 方法参数类型是Map，则提取Map的Value作为扩展名称。
            if (Map.class.isAssignableFrom(type)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) argument;
                for (String key : keys) {
                    String value = map.get(key).toString();
                    if (value != null) {
                        return value;
                    }
                }
                return null;
            }

            return null;
        }
    }
}
