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

package com.metaframe.cooma;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The information used by {@link ExtensionLoader} to generate Adaptive Instance.
 *
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 * @see ExtensionLoader
 * @see Config
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {

    /**
     * the key names of the {@link Config}, the corresponding value in {@link Config} used as the extension name which adaptive instance adapt to.
     * <br>
     * If these keys have no corresponding value in the {@link Config}, use the default extension(set by the {@link Extension} on the extension interface).
     * <p/>
     * eg. <code>String[] {"key1", "key2"}</code>, means:
     * <ol>
     * <li>use the value of <code>key1</code> as the extension name which adaptive instance adapt to;
     * <li>if <code>key1</code> has no corresponding value in the {@link Config}, use the value of <code>key2</code> as the extension name;
     * <li>if <code>key2</code> has no corresponding value in the {@link Config}, use the default extension;
     * <li>if has no default extension, throw {@link IllegalStateException} when inject the adaptive instance.
     * </ol>
     * <p/>
     * if default value of key names is the low case of extension interface name, separate word by dot.
     * eg. Extension interface <code>com.metaframe.cooma.FooBizService</code>, the key names is <code>String[] {"foo.biz.service"}</code>
     *
     * @see Extension#value()
     * @since 0.1.0
     */
    String[] value() default {};
}
